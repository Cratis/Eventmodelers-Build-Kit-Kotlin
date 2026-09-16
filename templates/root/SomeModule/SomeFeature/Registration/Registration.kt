package io.cratis.SomeModule.SomeFeature.Registration

import io.cratis.SomeModule.SomeFeature.SomeName
import io.cratis.arc.artifacts.Command
import io.cratis.arc.artifacts.CommandKey
import io.cratis.arc.authorization.AllowAnonymous
import io.cratis.chronicle.events.EventContext
import io.cratis.chronicle.events.EventType
import io.cratis.chronicle.observation.Reactor

/** Registers something by name. */
@Command
@AllowAnonymous
data class Register(
    @CommandKey val id: String,
    val name: String
) {
    /** Produces the event appended to the command-key event source. */
    fun handle(): Registered = Registered(SomeName(name))
}

/** Records that a registration happened. */
@EventType
data class Registered(val name: SomeName = SomeName(""))

/** Logs registrations as they happen. */
@Reactor
class RegistrationReactor {
    fun registered(event: Registered, context: EventContext) {
        println("Registered: ${event.name.value} (seq=${context.sequenceNumber})")
    }
}
