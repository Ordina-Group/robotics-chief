package nl.ordina.robotics.server.support

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import nl.ordina.robotics.server.socket.Command

inline fun <reified T : Command> Json.decodeCommand(value: JsonObject): T =
    this.decodeFromString<Command>(value.encode()) as T

inline fun <reified T> Json.decodeFromVertxJsonObject(value: JsonObject): T =
    this.decodeFromString<T>(value.encode())

inline fun <reified T> Json.encodeToVertxJsonObject(value: T): JsonObject {
    val element = encodeToJsonElement(value)

    if (element !is kotlinx.serialization.json.JsonObject) {
        throw IllegalArgumentException("Expected a Json Object, but got ${element::class}")
    }

    return element.toVertxJson()
}

inline fun <reified T : Iterable<*>> Json.encodeToVertxJsonArray(value: T): JsonArray {
    val element = encodeToJsonElement(value)

    if (element !is kotlinx.serialization.json.JsonArray) {
        throw IllegalArgumentException("Expected a Json Object, but got ${element::class}")
    }

    return element.toVertxJson()
}

@PublishedApi
internal fun kotlinx.serialization.json.JsonArray.toVertxJson(): JsonArray = this
    .map { v -> v.toObject() }
    .let { JsonArray(it) }

@PublishedApi
internal fun kotlinx.serialization.json.JsonObject.toVertxJson(): JsonObject = this
    .entries
    .associate { (k, v) -> k to v.toObject() }
    .let { JsonObject(it) }

private fun JsonElement.toObject(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> {
        if (isString) {
            contentOrNull
        } else {
            longOrNull
                ?: doubleOrNull
                ?: intOrNull
                ?: floatOrNull
                ?: booleanOrNull
                ?: contentOrNull
        }
    }

    is kotlinx.serialization.json.JsonObject -> toVertxJson()
    is kotlinx.serialization.json.JsonArray -> map { it.toObject() }
}
