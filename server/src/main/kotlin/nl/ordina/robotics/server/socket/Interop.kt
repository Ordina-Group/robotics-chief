package nl.ordina.robotics.server.socket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.support.encodeToVertxJsonObject

@PublishedApi
internal val logger = KotlinLogging.logger {}

fun io.vertx.core.eventbus.Message<JsonObject>.replyMessage(message: Message) {
    logger.trace { "Replying to ${this.address()}" }

    Json
        .encodeToVertxJsonObject<Message>(message)
        .let { reply(it) }
}

fun EventBus.publishMessage(address: String, message: Message): EventBus {
    logger.trace { "Publishing message to $address with body $message" }

    return Json
        .encodeToVertxJsonObject<Message>(message)
        .let { publish(address, it) }
}

fun <T> EventBus.requestCommand(address: String, message: Command): Future<io.vertx.core.eventbus.Message<T>> {
    logger.trace { "Publishing command to $address with body $message" }

    return Json
        .encodeToVertxJsonObject<Command>(message)
        .let { request(address, it) }
}
