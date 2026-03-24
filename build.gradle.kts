plugins {
    kotlin("jvm") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
    id("com.squareup.wire") version "5.5.1"
}

group = "app.krail.kgtfs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // Networking
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.0")
    implementation("io.ktor:ktor-client-core:3.4.0")
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation("io.ktor:ktor-client-okhttp:3.4.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.0")
    implementation("io.ktor:ktor-client-logging:3.4.0")
    implementation("io.ktor:ktor-client-auth:3.4.0")

    // IO
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.8.2")
    implementation("com.squareup.okio:okio:3.17.0")

    // Date Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1-0.6.x-compat")

    // https://ktor.io/docs/server-logging.html
    implementation("ch.qos.logback:logback-classic:1.5.27")

    // CSV
    implementation("com.jsoizo:kotlin-csv-jvm:1.10.0")

    /*implementation("com.squareup.wire:wire-runtime:4.4.3")
    implementation("com.squareup.wire:wire-grpc-client:4.4.3")
    implementation("com.squareup.wire:wire-moshi-adapter:4.4.3")
    implementation("com.squareup.wire:wire-schema:4.4.3")
    implementation("com.squareup.wire:wire-kotlin-generator:4.4.3")*/

    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
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
