package app.krail.kgtfs

import app.krail.kgtfs.filter.BusStopsFilter.filterOutBusStandData
import app.krail.kgtfs.io.NswStopsJsonIO.writeStopData
import app.krail.kgtfs.io.processParkRideData
import app.krail.kgtfs.nsw.NswGtfsManager
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    println("Welcome to KRAIL GTFS")

    runBlocking {
        NswGtfsManager.fetch(refresh = true)
            .let(::filterOutBusStandData)
            .let {
                writeStopData(it)
                processParkRideData(it)
            }
        exitProcess(0)
    }
}
