plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("com.squareup.wire") version "5.3.0"
}

group = "app.krail.kgtfs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Networking
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.1.1")
    implementation("io.ktor:ktor-client-core:3.1.1")
    implementation("io.ktor:ktor-client-cio:3.1.1")
    implementation("io.ktor:ktor-client-okhttp:3.1.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.1")
    implementation("io.ktor:ktor-client-logging:3.1.1")
    implementation("io.ktor:ktor-client-auth:3.1.1")

    // IO
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.6.0")
    implementation("com.squareup.okio:okio:3.10.2")

    // Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")

    // https://ktor.io/docs/server-logging.html
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // CSV
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")

    /*implementation("com.squareup.wire:wire-runtime:4.4.3")
    implementation("com.squareup.wire:wire-grpc-client:4.4.3")
    implementation("com.squareup.wire:wire-moshi-adapter:4.4.3")
    implementation("com.squareup.wire:wire-schema:4.4.3")
    implementation("com.squareup.wire:wire-kotlin-generator:4.4.3")*/

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion))
    }
}

tasks.register<JavaExec>("runKRAIL-GTFS") {
    group = "application"
    description = "Run the main class of the project"
    mainClass.set("app.krail.kgtfs.MainKt") // Replace with your main class
    classpath = sourceSets["main"].runtimeClasspath
}

wire {
    kotlin {
        javaInterop = true
        out = "$projectDir/build/generated/source/wire"
        rpcCallStyle = "suspending"
        rpcRole = "client"
        singleMethodServices = false
    }
    protoPath {
        srcDir("src/main/proto")
    }
    sourcePath {
        srcDir("src/main/proto")
    }
}
