---
name: build-state-change
description: >
  Implement Event Sourcing write slices the Cratis way — using Cratis Arc (CQRS) + Cratis Chronicle
  (event sourcing) in a Kotlin project. A write slice is: Command → handle() → Event(s), with
  optional validators and business rules. Use when: (1) implementing a new write
  slice / command in a Cratis project, (2) a slice.json has a non-empty commands[] / events[] section,
  (3) the user provides an Event Modeling artifact, specification, or natural-language description of a
  command and asks to implement it, (4) the user says "implement", "create", "add" a write slice,
  command, or state change in a Cratis Arc / Chronicle project.
---

# Cratis — Write Slice (State Change)

A write slice mutates state by recording events. In Cratis Arc the whole path lives in **one `.kt`
file**:

```
@Command data class + handle()  →  @EventType data class(es)  →  ./gradlew build  →  specs
```

> **Read first:** [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the
> non-negotiable Cratis rules. Everything below assumes them.

## Step 0 — Discover the target project's conventions

Before writing code, read the project's `CLAUDE.md` and **at least one existing slice** (the starter
ships one under `SomeModule/SomeFeature/`). Confirm:

- The package root (read `build.gradle.kts` and existing `.kt` files — never hard-code it; the
  package mirrors the folders).
- How existing commands return results (single event / `Pair` / `ArcOneOf` / no result).
- How `ConceptAs<T>` identity types are declared and where they live.
- Whether existing `.kt` files use a file header (the starter uses none).

> **Comments & description:** each slice element carries a `comments: string[]` array and a
> `description`. Use them as implementation hints. When done, resolve each used comment:
> `POST <BASE_URL>/api/org/<ORG_ID>/boards/<BOARD_ID>/nodes/<nodeId>/comments/<commentId>/resolve`
> (get IDs first via GET on the same path).

## Step 1 — Understand the input (`slice.json` is the source of truth)

Extract, regardless of input format:

| Element | What to extract |
|---|---|
| **Command** | Name (imperative), fields, which field is the event source id (`@CommandKey`) |
| **Events** | Names (past tense), fields, which events this command appends |
| **Business rules** | Preconditions, invariants, idempotency — from `description` / `comments` only |
| **State needed for rules** | Which read model must be inspected to evaluate a rule |
| **Specifications** | Each GWT / scenario maps 1:1 to an executable spec |

**If a field is not in `slice.json`, it does not go in the code.** If requirements are unclear, ask
the user before proceeding.

### Determine the trigger
If unclear how the command is dispatched, ask:
> - **UI / REST** — exposed automatically by Arc; add a React component + integration spec.
> - **Automation only** — dispatched internally by a reactor (no UI). The command still exists; no `.tsx`.

## Step 2 — Create concept types (if needed)

For any new identity / value, add one `ConceptAs<T>` per file in the feature folder:

```kotlin
package io.cratis.<Module>.<Feature>

import io.cratis.chronicle.concepts.ConceptAs

data class AuthorName(override val value: String) : ConceptAs<String>
```

See the shared conventions doc and [references/patterns.md](references/patterns.md).

## Step 3 — Write the slice `.kt` file

`<Module>/<Feature>/<Slice>/<Slice>.kt` — ALL backend artifacts in this one file; package
`io.cratis.<Module>.<Feature>.<Slice>` (no file header unless the project uses one).

### Events first
```kotlin
import io.cratis.chronicle.events.EventType

@EventType                                        // NEVER any arguments
data class AuthorRegistered(val name: AuthorName = AuthorName(""))
```
Past tense, no nullable properties, default values, one purpose each. If the context's events already
exist elsewhere, reuse them — don't redefine.

### Command with `handle()` on the class
```kotlin
import io.cratis.arc.artifacts.Command
import io.cratis.arc.artifacts.CommandKey

@Command
data class RegisterAuthor(
    @CommandKey val id: String,                  // event source id the event is appended to
    val name: AuthorName
) {
    fun handle(): AuthorRegistered = AuthorRegistered(name)   // Arc appends the returned event
}
```

Pick the return shape that matches the slice:
- **single event** → `fun handle(): EventName`
- **multiple values / responses** → `Pair` / `ArcOneOf` (Arc flattens the response; see
  `cratis-conventions.md`)
- **side-effect only** → no return value from `handle()`

Event source: the `@CommandKey` property is the event source id.

### Business rules — inject the read model
When a rule depends on event-sourced state, take the read model as an additional `handle()` parameter;
Arc injects current state. Only encode rules that appear in the slice `description` / `comments`. See
[references/patterns.md](references/patterns.md) for the read-model injection and error patterns.

## Step 4 — Build

From the project root: `./gradlew build`. Fix ALL warnings and errors before continuing. KSP generates
the Arc manifest (routes, contracts) as part of the build.

## Step 5 — Write specs (one per scenario in `slice.json`)

Put specs in `Tests/<Module>/<Feature>/<SliceName>Tests.kt`. Cover, from the slice's specifications:
- **Happy path** — command succeeds, correct event appended.
- **Each validation failure** — one spec per rule.
- **Each business-rule violation** — one spec per read-model condition.

Use `CommandScenario` from `io.cratis:arc-testing`:

```kotlin
val scenario = CommandScenario(module, RegisterAuthor::class.java)
scenario.execute(RegisterAuthor("author-1", AuthorName("Jane")))
    .shouldSucceed()
    .shouldHaveNoResponse()
val event = scenario.chronicle().shouldHaveAppendedEvent("author-1", AuthorRegistered::class.java)
```

Run `./gradlew test --tests "*<SliceName>*"`. Fix all failures.

## Step 6 — Frontend (only if the command is UI-triggered)

After the backend builds, add `<Module>/<Feature>/<Slice>/<Component>.tsx` importing the proxy from
`./`, using `CommandDialog` / inline form. Add a barrel `index.ts`. Register it in the feature's
composition page. See the shared conventions doc's React section and
[references/patterns.md](references/patterns.md).

## Final verification — does the implementation match `slice.json`?

- [ ] Every `commands[]` field → a Command property (no invented, none missing).
- [ ] Every `events[]` entry → an `@EventType` data class; names match exactly; fields match.
- [ ] Every specification / GWT scenario → an executable spec.
- [ ] No business rule in `handle()` that is absent from the slice `description` / `comments`.
- [ ] `./gradlew build` is clean (0 warnings / 0 errors); slice specs pass.

## References
- [references/patterns.md](references/patterns.md) — full command/event/read-model-injection code,
  specs, and the React command UI patterns.
- [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the Cratis conventions.
