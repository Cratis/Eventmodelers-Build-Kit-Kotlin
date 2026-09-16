---
name: build-state-view
description: >
  Implement read slices the Cratis way — an @ReadModel record fed by a Chronicle reducer or
  projection, exposed as an Arc query, plus the React component that renders it. Use when:
  (1) implementing a new read slice / projection in a Cratis project, (2) a slice.json has a
  non-empty readModel / projections / queries section, (3) the user provides a read-slice Event
  Modeling artifact or specification and asks to implement it, (4) the user says "implement",
  "create", "add" a read slice, read model, projection, reducer, or query in a Cratis Arc /
  Chronicle project.
---

# Cratis — Read Slice (State View)

A read slice projects events into a queryable read model. In Cratis the path lives in **one `.kt`
file**:

```
@ReadModel data class + @Path query  →  @Reducer (or @Projection)  →  ./gradlew build  →  React
```

> **Read first:** [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md). Everything
> below assumes those rules.

## Step 0 — Discover conventions

Read the project's `CLAUDE.md` and at least one existing read slice. Confirm the package root, how
read models expose queries, and how reducers are shaped.

## Step 1 — Understand the input (`slice.json` is the source of truth)

| Element | What to extract |
|---|---|
| **Read model** | Name, fields (from the events it is built from) |
| **Projections / queries** | Which events feed it, which queries expose it |
| **Specifications** | Each GWT / scenario maps 1:1 to an executable spec |

**If a field is not derivable from `slice.json` and the events, it does not go in the read model.**

## Step 2 — Write the read model + query in the slice `.kt` file

`<Module>/<Feature>/<Slice>/<Slice>.kt` — package `io.cratis.<Module>.<Feature>.<Slice>`:

```kotlin
import io.cratis.arc.artifacts.FromServices
import io.cratis.arc.artifacts.ReadModel as ArcReadModel
import io.cratis.arc.authorization.AllowAnonymous
import io.cratis.arc.queries.Path
import io.cratis.arc.queries.QueryContext
import io.cratis.chronicle.readModels.ReadModel as ChronicleReadModel

@ArcReadModel
@ChronicleReadModel
@AllowAnonymous
data class Listing(
    val id: String = "",
    val name: String = ""
) {
    companion object {
        @JvmStatic
        @Path("/api/<module>/<feature>/<slice>/all-listings")
        suspend fun allListings(
            context: QueryContext,
            @FromServices reader: ListingReader
        ): List<Listing> = reader.all(requireNamespace(context))
    }
}

/** Reads listings through the Chronicle read-model service. */
interface ListingReader {
    suspend fun all(namespace: String): List<Listing>
}
```

- Non-nullable properties with defaults.
- Queries are `companion object` functions with `@Path`, suspending, reading through a reader
  interface injected with `@FromServices` (the starter registers the Chronicle-backed reader bean).
- Register the reader bean in the application's Chronicle configuration.

## Step 3 — Write the reducer (or projection) in the same file

```kotlin
import io.cratis.chronicle.events.EventContext
import io.cratis.chronicle.observation.Reducer

@Reducer
class ListingReducer {
    fun registered(event: Registered, state: Listing?, context: EventContext): Listing = Listing(
        id = context.eventSourceId,
        name = event.name.value
    )
}
```

- One method per event, named after the event (camelCase).
- `state` is the current read model (nullable on first event); return the **new** state.
- Reducers join events, never read models. For declarative projections use `@Projection` +
  `IProjectionFor<T>` (see [references/patterns.md](references/patterns.md)).
- Remember to add new artifacts to the `KnownClientArtifacts` list in the Chronicle configuration.

## Step 4 — Build

From the project root: `./gradlew build`. Fix ALL warnings and errors.

## Step 5 — Write specs

Use `QueryScenario` from `io.cratis:arc-testing` in `Tests/<Module>/<Feature>/<SliceName>Tests.kt` —
one per scenario in `slice.json` (happy path, each projection rule, each query). Run
`./gradlew test --tests "*<SliceName>*"`.

## Step 6 — Frontend

After the backend builds, add the proxy import and `<Module>/<Feature>/<Slice>/<Component>.tsx`:
a DataTable using the observable query hook, fed by the generated proxy. Add a barrel `index.ts` and
register the component in the feature's composition page. See
[references/patterns.md](references/patterns.md).

## Final verification — does the implementation match `slice.json`?

- [ ] Read model fields match the events it is built from (no invented, none missing).
- [ ] Every `queries[]` entry → an executable query with `@Path`.
- [ ] Every `projections[]` / event mapping → a reducer method (or projection `from` clause).
- [ ] Every specification → an executable spec.
- [ ] `./gradlew build` is clean; slice specs pass.

## References
- [references/patterns.md](references/patterns.md) — reducer, projection, reader and React table
  patterns.
- [../_shared/cratis-conventions.md](../_shared/cratis-conventions.md) — the Cratis conventions.
