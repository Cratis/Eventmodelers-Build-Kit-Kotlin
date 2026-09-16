package io.cratis.SomeModule.SomeFeature

import io.cratis.SomeModule.SomeFeature.Registration.Register
import io.cratis.SomeModule.SomeFeature.Registration.Registered
import io.cratis.arc.artifacts.ArcArtifactModule
import io.cratis.arc.chronicle.chronicle
import io.cratis.arc.generated.CratisAppArcArtifactModule
import io.cratis.arc.testing.CommandScenario
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** Fast public-path contracts for the shipped example slice. */
class RegistrationTests {
    private val module: ArcArtifactModule = CratisAppArcArtifactModule()

    @Test
    fun `register appends the registered event`() = runBlocking {
        val scenario = CommandScenario(module, Register::class.java)
        scenario.execute(Register("listing-1", "Some Name"))
            .shouldSucceed()
            .shouldHaveNoResponse()

        val event = scenario.chronicle().shouldHaveAppendedEvent("listing-1", Registered::class.java)
        assertEquals("Some Name", event.name.value)
    }
}
