package app.krail.kgtfs

import app.krail.kgtfs.nsw.NswGtfsManager
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    println("Welcome to KRAIL GTFS")

    runBlocking {
        NswGtfsManager.fetch(refresh = true)
        buildUIFast()
        buildCLI()
        exitProcess(0)
    }
}

fun buildUIFast() {
    println("UI")
}

fun buildCLI() {
    println("CLI")
}