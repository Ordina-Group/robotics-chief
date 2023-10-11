package nl.ordina.robotics.server.web

import io.vertx.config.ConfigRetriever
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.bridge.PermittedOptions
import io.vertx.ext.stomp.BridgeOptions
import io.vertx.ext.stomp.StompServer
import io.vertx.ext.stomp.StompServerHandler
import io.vertx.ext.stomp.StompServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.awaitResult

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
        val stompServer = exposeSTOMPServer(config)

        server
            .requestHandler(router)
            .webSocketHandler(stompServer.webSocketHandler())
            .listen(config.getInteger("server.port", 8080))
    }

    private fun exposeSTOMPServer(config: JsonObject): StompServer {
        val options = StompServerOptions()
            .setPort(-1)
            .setWebsocketBridge(true)
            .setWebsocketPath(config.getString("server.socket.path", "/connect"))

        val serverHandler = StompServerHandler
            .create(vertx)
            .bridge(
                BridgeOptions()
                    .addInboundPermitted(PermittedOptions().setAddressRegex("/robots/\\d+/updates"))
                    .addOutboundPermitted(PermittedOptions().setAddressRegex("/robots/\\d+/updates")),
            )

        return StompServer
            .create(vertx, options)
            .handler(serverHandler)
    }
}

private inline fun <reified T> JsonObject.getListOf(key: String, def: List<String>): List<T> =
    this.getJsonArray(key, JsonArray(def)).toList().filterIsInstance<T>()
