# Write Slice Patterns (Kotlin)

Concrete code for the `build-state-change` skill. All snippets compile against the starter
(`io.cratis:arc-chronicle-spring-boot-starter`).

## 1. Concept (one per file, in the feature folder)

```kotlin
package io.cratis.SomeModule.SomeFeature

import io.cratis.chronicle.concepts.ConceptAs
import io.cratis.chronicle.concepts.EventSourceId

/** The name of an author. */
data class AuthorName(override val value: String) : ConceptAs<String>

/** Identifies an author; the event source id every author event is appended against. */
data class AuthorId(override val value: String) : EventSourceId
```

## 2. Simplest write slice — single event

```kotlin
package io.cratis.SomeModule.SomeFeature.Registration

import io.cratis.arc.artifacts.Command
import io.cratis.arc.artifacts.CommandKey
import io.cratis.arc.authorization.AllowAnonymous
import io.cratis.chronicle.events.EventType

@EventType
data class AuthorRegistered(val name: AuthorName = AuthorName(""))

@Command
@AllowAnonymous
data class RegisterAuthor(
    @CommandKey val id: String,
    val name: AuthorName
) {
    fun handle(): AuthorRegistered = AuthorRegistered(name)
}
```

## 3. Multiple values / alternative outcomes

Arc flattens `Pair`, `Triple` and `ArcOneOf` return values into the command response — see
`cratis-conventions.md` for the response shapes the current version supports.

## 4. Business rule — inject a read model

```kotlin
@Command
data class RegisterAuthor(
    @CommandKey val id: String,
    val name: AuthorName
) {
    fun handle(existing: AuthorByName?): AuthorRegistered? =
        if (existing != null) null else AuthorRegistered(name)
}
```

Only encode rules that appear in the slice `description` / `comments`.

## 5. Spec — CommandScenario (from `io.cratis:arc-testing`)

```kotlin
package io.cratis.SomeModule.SomeFeature

import io.cratis.arc.artifacts.ArcArtifactModule
import io.cratis.arc.chronicle.chronicle
import io.cratis.arc.generated.CratisAppArcArtifactModule
import io.cratis.arc.testing.CommandScenario
import io.cratis.SomeModule.SomeFeature.Registration.AuthorRegistered
import io.cratis.SomeModule.SomeFeature.Registration.RegisterAuthor
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegistrationTests {
    private val module: ArcArtifactModule = CratisAppArcArtifactModule()

    @Test
    fun `registers an author`() = runBlocking {
        val scenario = CommandScenario(module, RegisterAuthor::class.java)
        scenario.execute(RegisterAuthor("author-1", AuthorName("Jane")))
            .shouldSucceed()
            .shouldHaveNoResponse()
        val event = scenario.chronicle().shouldHaveAppendedEvent("author-1", AuthorRegistered::class.java)
        assertEquals("Jane", event.name.value)
    }
}
```

## 6. React command UI (after the backend built)

```tsx
import { CommandDialog } from '@cratis/components/CommandDialog';
import { InputTextField } from '@cratis/components/CommandForm';
import { RegisterAuthor } from './RegisterAuthor';

export const RegisterAuthorDialog = () => (
    <CommandDialog command={RegisterAuthor} title='Register author' okLabel='Register'>
        <InputTextField<RegisterAuthor> value={c => c.name} title='Name' />
    </CommandDialog>
);
```

## Checklist

- [ ] Events past tense, `@EventType` with no arguments, defaults, never nullable.
- [ ] `@CommandKey` carries the event source id.
- [ ] `handle()` on the command class — never a separate handler.
- [ ] Spec per scenario in `slice.json`.
