# Automation / Translation Patterns (Kotlin)

Concrete code for the `build-automation` skill. All snippets compile against the starter.

## 1. Automation — react with a side effect

```kotlin
package io.cratis.SomeModule.SomeFeature.Registration

import io.cratis.chronicle.events.EventContext
import io.cratis.chronicle.observation.Reactor

@Reactor
class RegistrationReactor {
    fun registered(event: Registered, context: EventContext) {
        println("Registered: ${event.name.value} (seq=${context.sequenceNumber})")
    }
}
```

## 2. Translation — event → event on another stream

```kotlin
import io.cratis.chronicle.eventSequences.EventForEventSourceId

@Reactor
class AuditReactor {
    fun registered(event: Registered, context: EventContext): EventForEventSourceId =
        EventForEventSourceId(
            "audit-log",
            RegistrationAudited(context.eventSourceId)
        )
}
```

## 3. Same-stream follow-up event

```kotlin
@Reactor
class WelcomeReactor {
    fun registered(event: Registered, context: EventContext): WelcomeEmailSent =
        WelcomeEmailSent(context.eventSourceId)
}
```

## 4. Multiple handlers in one reactor (same concern)

```kotlin
@Reactor
class RegistrationReactor {
    fun registered(event: Registered, context: EventContext) { /* … */ }
    fun registrationAmended(event: RegistrationAmended, context: EventContext) { /* … */ }
}
```

Dispatch is by the event parameter type — one method per event, named after the event.

## 5. Idempotency

Reactors may run again for the same event. Never assume once-only delivery: derive side effects from
the event payload (not from external mutable state), and make returned events self-describing.

## Checklist

- [ ] One reactor method per observed event, named after the event.
- [ ] Reactor is idempotent and stateless.
- [ ] Artifacts registered in `KnownClientArtifacts`.
- [ ] Spec per scenario in `slice.json`.
