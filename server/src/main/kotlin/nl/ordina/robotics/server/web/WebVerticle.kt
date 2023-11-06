package nl.ordina.robotics.server.web

import io.vertx.config.ConfigRetriever
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.stomp.BridgeOptions
import io.vertx.ext.stomp.Command
import io.vertx.ext.stomp.DefaultSubscribeHandler
import io.vertx.ext.stomp.Frame
import io.vertx.ext.stomp.ServerFrame
import io.vertx.ext.stomp.StompServer
import io.vertx.ext.stomp.StompServerHandler
import io.vertx.ext.stomp.StompServerOptions
import io.vertx.ext.stomp.utils.Headers
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.socket.logger

class WebVerticle : CoroutineVerticle() {
    override suspend fun start() {
        val config = awaitResult<JsonObject> {
            ConfigRetriever
                .create(vertx)
                .config
                .onComplete(it)
        }

        initializeWebServer(config)
        hostStaticContent(config)
    }

    private fun hostStaticContent(config: JsonObject): Router {
        val router = Router.router(vertx)
        val handler = StaticHandler
            .create(
                config.getString(
                    "server.web.static-folder",
                    "./build/resources/main",
                ),
            )
            .apply {
                setIndexPage("index.html")
            }
        router.route("/*").handler(handler)

        return router
    }

    private fun initializeWebServer(config: JsonObject) {
        val server = vertx.createHttpServer(
            HttpServerOptions().setWebSocketSubProtocols(
                config.getListOf<String>(
                    "server.socket.stomp.versions",
                    listOf("v10.stomp", "v11.stomp", "v12.stomp"),
                ),
            ),
        )

        val router = hostStaticContent(config)
        try {
            val stompServer = createSTOMPServer(config)

            server
                .requestHandler(router)
                .webSocketHandler(stompServer.webSocketHandler())
                .listen(config.getInteger("server.port", 8080))
        } catch (e: Exception) {
            logger.error { "Failed to start web server: ${e.message}" }
        }
    }

    private fun createSTOMPServer(config: JsonObject): StompServer {
        val options = StompServerOptions()
            .setPort(-1)
            .setWebsocketBridge(true)
            .setWebsocketPath(config.getString("server.socket.path", "/connect"))

        val serverHandler = StompServerHandler
            .create(vertx)
            .bridge(
                BridgeOptions()
                    .addInboundPermitted(PermittedOptions().setAddressRegex(Addresses.Boundary.inboundPermitted()))
                    .addOutboundPermitted(PermittedOptions().setAddressRegex(Addresses.Boundary.outboundPermitted())),
            )
            .subscribeHandler(subscribeHandlerWithInitialStateSupport())

        return StompServer
            .create(vertx, options)
            .handler(serverHandler)
    }

    private fun subscribeHandlerWithInitialStateSupport(): Handler<ServerFrame> {
        val defaultHandler = DefaultSubscribeHandler()
        val eventBus = vertx.eventBus()

        return Handler<ServerFrame> { serverFrame ->
            val subscriptionId = serverFrame.frame().getHeader(Frame.ID)!!
            val slice = serverFrame.frame().destination!!
            logger.debug { "Received subscribe request for $slice" }

            eventBus.consumer<JsonObject>(Addresses.initialSlice(subscriptionId)) {
                logger.debug { "Sending initial state $slice: ${it.body()}" }

                if (it.body()?.isEmpty != false) {
                    return@consumer
                }

                try {
                    val body = it.body()?.toBuffer()
                    val headers: Headers =
                        Headers.create()
                            .add(Frame.DESTINATION, slice)
                            .add(Frame.SUBSCRIPTION, subscriptionId)
                            .add(Frame.CONTENT_LENGTH, (body?.length() ?: 0).toString())
                    val message = Frame(
                        Command.MESSAGE,
                        headers,
                        body,
                    )

                    serverFrame.connection().write(message)
                } catch (e: Exception) {
                    logger.error { "Failed to send initial state $slice: ${e.message}" }
                }
            }

            eventBus.publish(
                Addresses.initialSlice(),
                JsonObject.of(
                    "slice",
                    slice,
                    "subscriptionId",
                    subscriptionId,
                ),
            )

            defaultHandler.handle(serverFrame)
        }
    }
}

private inline fun <reified T> JsonObject.getListOf(key: String, def: List<String>): List<T> =
    this.getJsonArray(key, JsonArray(def)).toList().filterIsInstance<T>()
