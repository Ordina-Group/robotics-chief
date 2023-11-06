package nl.ordina.robotics.server.support

import io.vertx.core.buffer.Buffer
import io.vertx.core.eventbus.MessageCodec
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import nl.ordina.robotics.server.transport.cli.InstructionResult

class InstructionResultCodec : MessageCodec<InstructionResult, InstructionResult> {
    override fun name(): String = "smurfinstructionresult"

    override fun systemCodecID(): Byte = -1

    override fun encodeToWire(buffer: Buffer, instruction: InstructionResult) {
        val encoded = Json.encodeToJsonElement(instruction).jsonObject.toVertxJson().toBuffer()
        buffer.appendInt(encoded.length())
        buffer.appendBuffer(encoded)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun decodeFromWire(pos: Int, buffer: Buffer): InstructionResult {
        var pos = pos
        val length = buffer.getInt(pos)
        pos += 4
        val data = buffer.slice(pos, pos + length)

        return Json.decodeFromStream<InstructionResult>(data.bytes.inputStream())
    }

    override fun transform(instruction: InstructionResult): InstructionResult = instruction
}
