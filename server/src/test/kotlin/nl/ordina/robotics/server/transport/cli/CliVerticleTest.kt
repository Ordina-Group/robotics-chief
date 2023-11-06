package nl.ordina.robotics.server.transport.cli

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.serialization.json.Json
import nl.ordina.robotics.server.bus.Addresses
import nl.ordina.robotics.server.support.decodeFromVertxJsonObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class CliVerticleTest {
    @BeforeEach
    fun deploy_verticle(vertx: Vertx, testContext: VertxTestContext) {
        val options = DeploymentOptions().setConfig(JsonObject().put("robot.id", "3"))
        vertx.deployVerticle(CliVerticle(), options, testContext.succeedingThenComplete())
    }

    @Test
    fun test_receive_commands(vertx: Vertx, testContext: VertxTestContext) {
        val command = JsonObject().put("type", "Command.GetWifiNetworks")
        val eb = vertx.eventBus()

        // Set up network
        eb.consumer(Addresses.Network.executeInstruction("3")) {
            val instructionSet = Json.decodeFromVertxJsonObject<Instruction>(it.body())
            assert(instructionSet.value.size == 2)
            it.reply(JsonObject().put("network", "response"))
        }

        // Set up application listener
        vertx.eventBus().request<JsonObject>(Addresses.Transport.execute("3"), command) {
            assert(it.succeeded())
            assert(it.result().body().getString("network") == "response")
            testContext.completeNow()
        }
    }
}
