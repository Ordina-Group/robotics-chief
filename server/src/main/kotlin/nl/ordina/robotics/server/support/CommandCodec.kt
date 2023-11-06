package nl.ordina.robotics.server.support

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.ordina.robotics.server.socket.Command

class CommandCodec : MessageCodec<Command, Command> {
    override fun name(): String = "smurfcommand"

    override fun systemCodecID(): Byte = -1

    override fun encodeToWire(buffer: Buffer, command: Command) {
        val encoded = Json.encodeToJsonElement(command).jsonObject.toVertxJson().toBuffer()
        buffer.appendInt(encoded.length())
        buffer.appendBuffer(encoded)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeFromWire(pos: Int, buffer: Buffer): Command {
        var pos = pos
        val length = buffer.getInt(pos)
        pos += 4
        val data = buffer.slice(pos, pos + length)

        return Json.decodeFromStream<Command>(data.bytes.inputStream())
    }

    override fun transform(command: Command): Command = command
}
