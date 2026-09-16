package io.cratis.SomeModule.SomeFeature.Listing

import io.cratis.SomeModule.SomeFeature.Registration.Registered
import io.cratis.SomeModule.SomeFeature.SomeName
import io.cratis.arc.artifacts.FromServices
import io.cratis.arc.artifacts.ReadModel as ArcReadModel
import io.cratis.arc.authorization.AllowAnonymous
import io.cratis.arc.chronicle.TenantEventStoreResolver
import io.cratis.arc.queries.Path
import io.cratis.arc.queries.QueryContext
import io.cratis.chronicle.events.EventContext
import io.cratis.chronicle.observation.Reducer
import io.cratis.chronicle.readModels.ReadModel as ChronicleReadModel

/** Registrations materialized by Chronicle and exposed through generated Arc queries. */
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

/** Reads listings from one explicit Chronicle namespace. */
interface ListingReader {
    suspend fun all(namespace: String): List<Listing>
}

internal class ChronicleListingReader(
    private val eventStoreResolver: TenantEventStoreResolver
) : ListingReader {
    override suspend fun all(namespace: String): List<Listing> =
        eventStore(namespace).readModels.getInstances(Listing::class)

    private fun eventStore(namespace: String) = requireNotNull(eventStoreResolver.resolve(namespace)) {
        "Chronicle event store is unavailable for tenant namespace '$namespace'."
    }
}

/** Builds [Listing] from [Registered] events. */
@Reducer
class ListingReducer {
    fun registered(event: Registered, state: Listing?, context: EventContext): Listing = Listing(
        id = context.eventSourceId,
        name = event.name.value
    )
}
