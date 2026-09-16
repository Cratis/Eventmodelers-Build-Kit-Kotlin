# Read Slice Patterns (Kotlin)

Concrete code for the `build-state-view` skill. All snippets compile against the starter.

## 1. Read model + query (reader interface)

```kotlin
package io.cratis.SomeModule.SomeFeature.Listing

import io.cratis.arc.artifacts.FromServices
import io.cratis.arc.artifacts.ReadModel as ArcReadModel
import io.cratis.arc.authorization.AllowAnonymous
import io.cratis.arc.chronicle.TenantEventStoreResolver
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
        @Path("/api/some-module/some-feature/listing/all-listings")
        suspend fun allListings(
            context: QueryContext,
            @FromServices reader: ListingReader
        ): List<Listing> = reader.all(requireNamespace(context))

        private fun requireNamespace(context: QueryContext): String =
            requireNotNull(context.tenantNamespace) { "A tenant namespace is required." }
    }
}

interface ListingReader {
    suspend fun all(namespace: String): List<Listing>
}

internal class ChronicleListingReader(
    private val eventStoreResolver: TenantEventStoreResolver
) : ListingReader {
    override suspend fun all(namespace: String): List<Listing> =
        requireNotNull(eventStoreResolver.resolve(namespace)) { "Event store unavailable." }
            .readModels.getInstances(Listing::class)
}
```

Register the reader bean in the application's Chronicle configuration.

## 2. Reducer (running state)

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

## 3. Declarative projection (simple property mapping)

```kotlin
import io.cratis.chronicle.projections.IProjectionBuilderFor
import io.cratis.chronicle.projections.IProjectionFor
import io.cratis.chronicle.projections.Projection

@Projection
class ListingProjection : IProjectionFor<Listing> {
    override fun define(builder: IProjectionBuilderFor<Listing>) {
        builder
            .from(Registered::class)
    }
}
```

## 4. Spec — QueryScenario

```kotlin
val byId = module.queryPerformers.single { it.fullyQualifiedName.value.endsWith(".allListings") }
QueryScenario<Listing>(byId)
    .addService(ListingReader::class.java, reader)
    .withTenant("tenant-a")
    .perform(emptyMap())
    .shouldSucceed()
    .shouldHaveData(expected)
```

## 5. React — render the query proxy

```tsx
import { AllListings } from './AllListings';

export const ListingDataTable = () => {
    const [result] = AllListings.use();
    return <DataTable value={result.data} />;
};
```

## Checklist

- [ ] Read model non-nullable properties with defaults.
- [ ] Query is a `companion object` function with `@Path`, reading through the reader interface.
- [ ] Reducer/projection joins events, never read models.
- [ ] Spec per scenario in `slice.json`.
