package io.cratis

import io.cratis.SomeModule.SomeFeature.Listing.ChronicleListingReader
import io.cratis.SomeModule.SomeFeature.Listing.Listing
import io.cratis.SomeModule.SomeFeature.Listing.ListingReader
import io.cratis.SomeModule.SomeFeature.Listing.ListingReducer
import io.cratis.SomeModule.SomeFeature.Registration.Register
import io.cratis.SomeModule.SomeFeature.Registration.Registered
import io.cratis.SomeModule.SomeFeature.Registration.RegistrationReactor
import io.cratis.arc.chronicle.TenantEventStoreResolver
import io.cratis.chronicle.ChronicleOptions
import io.cratis.chronicle.artifacts.KnownClientArtifacts
import io.cratis.chronicle.connection.ChronicleConnectionString
import io.cratis.chronicle.spring.ChronicleProperties
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/** Bridges Arc and Chronicle for the shipped example slices. */
@Configuration(proxyBeanMethods = false)
class ChronicleConfiguration {
    /** Uses an explicit artifact list so executable Spring Boot jars register the same domain contract. */
    @Bean
    fun chronicleOptions(
        properties: ChronicleProperties,
        @Value("\${spring.application.name:Unknown}") applicationName: String
    ): ChronicleOptions = ChronicleOptions(
        connectionString = ChronicleConnectionString.parse(properties.connectionString),
        programIdentifier = properties.programIdentifier ?: applicationName,
        defaultSinkTypeId = properties.defaultSinkTypeId
            ?: System.getenv("CHRONICLE_SINK_TYPE")
            ?: "InMemory",
        autoDiscoverAndRegister = properties.autoDiscoverAndRegister,
        artifacts = KnownClientArtifacts(
            Register::class,
            Registered::class,
            RegistrationReactor::class,
            Listing::class,
            ListingReducer::class
        )
    )

    /** Supplies generated queries with explicit namespace-aware Chronicle reads. */
    @Bean
    fun listingReader(resolver: TenantEventStoreResolver): ListingReader = ChronicleListingReader(resolver)
}
