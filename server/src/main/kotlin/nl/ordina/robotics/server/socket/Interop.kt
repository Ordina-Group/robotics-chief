package nl.ordina.robotics.server.socket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.vertx.core.Future
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.support.encodeToVertxJsonObject

@PublishedApi
internal val logger = KotlinLogging.logger {}

private val json = Json { encodeDefaults = true }

private val messageDeliveryOptions = DeliveryOptions().setCodecName("smurfmessage")
private val commandDeliveryOptions = DeliveryOptions().setCodecName("smurfcommand")

fun io.vertx.core.eventbus.Message<*>.replyMessage(message: Message) {
    logger.trace { "Replying to ${this.address()}" }

    reply(message, messageDeliveryOptions)
}

fun io.vertx.core.eventbus.Message<*>.replyBoundaryMessage(message: Message?) {
    logger.trace { "Replying message to boundary with body $message" }
    val encoded = message?.let { json.encodeToVertxJsonObject<Message>(it) }

    return reply(encoded)
}

fun EventBus.publishBoundaryMessage(address: String, message: Message?): EventBus {
    logger.trace { "Publishing message to boundary $address with body $message" }

    return message
        ?.let { json.encodeToVertxJsonObject<Message>(it) }
        .let { publish(address, it) }
}

fun EventBus.publishMessage(address: String, message: Message): EventBus {
    logger.trace { "Publishing message to $address with body $message" }

    return publish(address, message, messageDeliveryOptions)
}

fun <T> EventBus.requestCommand(address: String, message: Command): Future<io.vertx.core.eventbus.Message<T>> {
    logger.trace { "Publishing command to $address with body $message" }

    return request(address, message, commandDeliveryOptions)
}
