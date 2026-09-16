# Kotlin App — agent conventions

This is a **Cratis** application: Cratis Arc (CQRS) + Cratis Chronicle (event sourcing) in Kotlin.

Build slices with the kit's skills — `/build-state-change`, `/build-state-view`, `/build-automation` —
and follow the conventions in `.cratis-build-kit/.claude/skills/_shared/cratis-conventions.md`.

## Structure

```
<Module>/<Feature>/
├── <Module>.kt           ← module definition
├── <Feature>.kt          ← feature definition
└── <Slice>/              ← one slice = one behavior
    ├── <Slice>.kt        ← ALL backend artifacts for the slice in ONE file
    └── index.kt          ← barrel export
```

- Top folder is a **module**, then a **feature**, then **slices**.
- **Slices are implemented in ONE `.kt` file** — match the pattern in `SomeModule/SomeFeature/SomeSlice.kt`.

## Non-negotiables

- ALL backend artifacts for a slice live in ONE `.kt` file.
- `[Command]` records define `Handle()` directly — never separate handler classes.
- `[EventType]` takes NO arguments; events are past-tense and never nullable.
- `ConceptAs<T>` for identity/value types — no raw `UUID` / `String` in the domain.
- Reactors implement `IReactor`; dispatch is by the first parameter type. Write new events
  only via `ICommandPipeline.Execute(...)`, never `IEventLog`.

## Build, run, test

```bash
./gradlew build    # compiles backend
./gradlew test     # run specs (filter while iterating: --filter <Slice>)
```

Full detail: `.cratis-build-kit/.claude/skills/_shared/cratis-conventions.md`.
