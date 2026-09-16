package io.cratis

import io.cratis.arc.kotlin.*
import io.cratis.chronicle.kotlin.*

fun main() {
    val host = createArcHost {
        // Configure your Arc host here
    }
    
    host.start()
}
