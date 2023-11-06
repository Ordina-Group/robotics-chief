package nl.ordina.robotics.server.support

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream

class KotlinxSerializationCodec : MessageCodec<JsonObject, JsonObject> {
    override fun name(): String = "kotlinxjsonobject"

    override fun systemCodecID(): Byte = -1

    override fun encodeToWire(buffer: Buffer, jsonObject: JsonObject) {
        val encoded = jsonObject.toVertxJson().toBuffer()
        buffer.appendInt(encoded.length())
        buffer.appendBuffer(encoded)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeFromWire(pos: Int, buffer: Buffer): JsonObject {
        var pos = pos
        val length = buffer.getInt(pos)
        pos += 4
        val data = buffer.slice(pos, pos + length)

        return Json.decodeFromStream<JsonObject>(data.bytes.inputStream())
    }

    override fun transform(jsonObject: JsonObject): JsonObject = jsonObject
}
