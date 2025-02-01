package app.krail.kgtfs

import app.krail.kgtfs.nsw.NswGtfsManager
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    println("Welcome to KRAIL GTFS")

    runBlocking {
        NswGtfsManager.fetch(refresh = true,)
        exitProcess(0)
    }
}
