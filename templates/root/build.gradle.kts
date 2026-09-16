plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "io.cratis"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("io.cratis.KotlinAppKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.cratis:chronicle-client-kotlin:latest")
    implementation("io.cratis:arc-kotlin:latest")
    testImplementation(kotlin("test"))
}
