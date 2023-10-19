package nl.ordina.robotics.server.support

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

inline fun <reified T> Json.decodeFromVertxJsonObject(value: JsonObject, prefix: String? = null): T {
    val config = if (prefix != null) {
        value
            .filter { it.key.startsWith("$prefix.") }
            .associate { (key, value) -> key.removePrefix("$prefix.") to value }
            .let(::JsonObject)
    } else {
        value
    }

    return this.decodeFromString<T>(config.encode())
}

inline fun <reified T> Json.encodeToVertxJsonObject(value: T): JsonObject {
    val element = encodeToJsonElement(value)

    if (element !is kotlinx.serialization.json.JsonObject) {
        throw IllegalArgumentException("Expected a Json Object, but got ${element::class}")
    }

    return element.toVertxJson()
}

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
