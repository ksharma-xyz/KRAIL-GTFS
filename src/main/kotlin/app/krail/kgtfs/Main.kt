package app.krail.kgtfs

import app.krail.kgtfs.filter.BusStopsFilter.filterOutBusStandData
import app.krail.kgtfs.filter.SydneyFerryFilter.processSydneyFerryData
import app.krail.kgtfs.io.NswStopsJsonIO.writeStopData
import app.krail.kgtfs.io.writeParkRideData
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.NswGtfsManager
import app.krail.kgtfs.nsw.NswTransportModeType
import app.krail.kgtfs.routes.RouteProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() {
    println("Welcome to KRAIL GTFS")

    runBlocking {
        // Fetch GTFS data
        val nswData = NswGtfsManager.fetch(refresh = true)

        // Process stops and routes in parallel
        coroutineScope {
            val stopsJob = async { processStops(nswData) }
            val routesJob = async { processRoutes() }

            stopsJob.await()
            routesJob.await()
        }

        exitProcess(0)
    }
}

suspend fun processStops(nswData: List<StopJson>) {
    nswData
        .let { filterOutBusStandData(it, preserveChildren = true) }
        .let(::processSydneyFerryData)
        .let {
            writeStopData(it)
            writeParkRideData(it)
        }
}

suspend fun processRoutes() {
    RouteProcessor.processAndExport(
        transportMode = NswTransportModeType.BUSES,
        exportToJson = true,
        exportToProtobuf = true
    )
}
