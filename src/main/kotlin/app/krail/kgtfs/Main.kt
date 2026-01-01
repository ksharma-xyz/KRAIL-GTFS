package app.krail.kgtfs

import app.krail.kgtfs.filter.BusStopsFilter.filterOutBusStandData
import app.krail.kgtfs.filter.SydneyFerryFilter.processSydneyFerryData
import app.krail.kgtfs.io.NswStopsJsonIO.writeStopData
import app.krail.kgtfs.io.writeParkRideData
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
            // Stop processing
            val stopsJob = async {
                nswData
                    .let(::filterOutBusStandData)
                    .let(::processSydneyFerryData)
                    .let {
                        writeStopData(it)
                        writeParkRideData(it)
                    }
            }

            // Route processing (parallel) - exports both JSON and Protobuf
            val routesJob = async {
                RouteProcessor.processAndExport(
                    transportMode = NswTransportModeType.BUSES,
                    exportToJson = true,
                    exportToProtobuf = true
                )
            }

            // Wait for both to complete
            stopsJob.await()
            routesJob.await()
        }

        exitProcess(0)
    }
}
