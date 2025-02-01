plugins {
    kotlin("jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

group = "app.krail.kgtfs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Networking
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-cio:3.0.3")
    implementation("io.ktor:ktor-client-okhttp:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
    implementation("io.ktor:ktor-client-logging:3.0.3")
    implementation("io.ktor:ktor-client-auth:3.0.3")

    // IO
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
    implementation("com.squareup.okio:okio:3.10.2")

    // Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    // https://ktor.io/docs/server-logging.html
    implementation("ch.qos.logback:logback-classic:1.4.12")

    // CSV
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.register<JavaExec>("runKRAIL-GTFS") {
    group = "application"
    description = "Run the main class of the project"
    mainClass.set("app.krail.kgtfs.MainKt") // Replace with your main class
    classpath = sourceSets["main"].runtimeClasspath
}