package nl.ordina.robotics.server.support

import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitResult

suspend fun Vertx.loadConfig(): JsonObject = awaitResult<JsonObject> {
    ConfigRetriever
        .create(this)
        .config
        .onComplete(it)
}
