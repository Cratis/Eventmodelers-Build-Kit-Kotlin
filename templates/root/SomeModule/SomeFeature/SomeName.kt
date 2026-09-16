package io.cratis.SomeModule.SomeFeature

import io.cratis.chronicle.concepts.ConceptAs

/** A name given a type of its own, so it cannot be passed where another String concept is expected. */
data class SomeName(override val value: String) : ConceptAs<String>
