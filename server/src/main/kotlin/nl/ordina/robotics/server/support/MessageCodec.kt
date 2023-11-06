package nl.ordina.robotics.server.support

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.ordina.robotics.server.socket.Message

class MessageCodec : MessageCodec<Message, Message> {
    override fun name(): String = "smurfmessage"

    override fun systemCodecID(): Byte = -1

    override fun encodeToWire(buffer: Buffer, message: Message) {
        val encoded = Json.encodeToJsonElement(message).jsonObject.toVertxJson().toBuffer()
        buffer.appendInt(encoded.length())
        buffer.appendBuffer(encoded)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeFromWire(pos: Int, buffer: Buffer): Message {
        var pos = pos
        val length = buffer.getInt(pos)
        pos += 4
        val data = buffer.slice(pos, pos + length)

        return Json.decodeFromStream<Message>(data.bytes.inputStream())
    }

    override fun transform(message: Message): Message = message
}
