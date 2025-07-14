package app.krail.kgtfs

import app.krail.kgtfs.filter.BusStopsFilter.filterOutBusStandData
import app.krail.kgtfs.filter.SydneyFerryFilter.processSydneyFerryData
import app.krail.kgtfs.io.NswStopsJsonIO.writeStopData
import app.krail.kgtfs.io.writeParkRideData
import app.krail.kgtfs.nsw.NswGtfsManager
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    println("Welcome to KRAIL GTFS")

    runBlocking {
        NswGtfsManager.fetch(refresh = true)
            .let(::filterOutBusStandData)
            .let(::processSydneyFerryData)
            .let {
                writeStopData(it)
                writeParkRideData(it)
            }
        exitProcess(0)
    }
}
