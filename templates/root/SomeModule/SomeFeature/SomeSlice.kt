package io.cratis.SomeModule.SomeFeature

import io.cratis.chronicle.kotlin.*

/**
 * Example slice demonstrating the Cratis way in Kotlin.
 * 
 * This is a state change slice that implements a command.
 * Follow this pattern for all slices.
 */
@Command
data class Register(
    val id: Guid,
    val name: String
) {
    class Handle : IReactor<Register> {
        fun invoke(command: Register, context: IEventContext) {
            // Implement your command handling logic here
            context.publish(SomeEvent(command.id, command.name))
        }
    }
}

data class SomeEvent(
    val id: Guid,
    val name: String
) : IEvent
