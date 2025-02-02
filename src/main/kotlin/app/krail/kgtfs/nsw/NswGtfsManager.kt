package app.krail.kgtfs.nsw

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.network.cacheDirPath
import app.krail.kgtfs.nsw.NswTransport.fetchAndProcessNswTransportData
import app.krail.kgtfs.proto.KrailNswStop
import app.krail.kgtfs.proto.KrailNswStopList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

object NswGtfsManager {

    /**
     * Fetches and processes GTFS data for all NSW transport modes.
     * Will save the stops.txt data as [StopJson] and convert to json file inside cache directory.
     *
     * @param refresh if true, will fetch the data from the network, otherwise will use the cached data
     */
    suspend fun fetch(
        refresh: Boolean = true,
        modes: List<NswTransportModeType> = NswTransportModeType.entries.filterNot { it == NswTransportModeType.COACH }
    ) = withContext(Dispatchers.Default) {

        val gtfsStopMap = modes.associateWith { mode ->
            mode.fetchAndProcessNswTransportData(fetchFromNetwork = refresh)
        }.toSortedMap()

        println("Map Order: ${gtfsStopMap.keys}")

        val result: List<StopJson> = createCommonGtfsStops(gtfsStopMap)
        writeStopData(result)
    }

    /**
     * Creates a common list of stops from multiple GTFS sources.
     * Merges stops with the same stopId and combines their product classes.
     */
    fun createCommonGtfsStops(gtfsStopMap: Map<NswTransportModeType, List<GtfsStop>>): MutableList<StopJson> {
        val allStops = mutableListOf<StopJson>()

        gtfsStopMap.forEach { (mode, stopList) ->
            stopList.forEach { gtfsStop ->
                val existingStop = allStops.find { it.id == gtfsStop.stopId.id }
                if (existingStop != null) {
                    // Stop already exists, add the product class to the existing stop.
                    if (gtfsStop.name.contains("Coach Stop") || existingStop.name.contains("Coach Stop")) {
                        // println("Coach Stop: ${gtfsStop.name} - ${existingStop.name}")
                        existingStop.productClass.add(NswTransportModeType.COACH.productClass)
                    } else {
                        existingStop.productClass.add(mode.productClass)
                    }

                    existingStop.name = gtfsStop.name
                } else {
                    if (gtfsStop.name.contains("Coach Stop")) {
                        // println("Coach Stop: ${gtfsStop.name}")
                        allStops.add(
                            gtfsStop.toStopJson(mode).copy(
                                productClass = mutableSetOf(NswTransportModeType.COACH.productClass)
                            )
                        )
                    } else {
                        allStops.add(gtfsStop.toStopJson(mode))
                    }
                }
            }
        }
        return allStops
    }

    // Replace hardcoded file-writing logic with calls to writeJsonToFile
    private suspend fun writeStopData(result: List<StopJson>) = withContext(Dispatchers.IO) {
        // Write as pretty JSON
        writeJsonToFile(
            data = result,
            path = cacheDirPath,
            fileName = "NSW_STOPS",
            pretty = true,
        )

        // Write as compact JSON
        writeJsonToFile(
            data = result,
            path = cacheDirPath,
            fileName = "NSW_STOPS",
            pretty = false,
        )

        // Write as Protobuf binary
        val stopList = result.map { stop ->
            KrailNswStop(
                stopId = stop.id,
                stopName = stop.name,
                lat = stop.lat.toDouble(),
                lon = stop.lon.toDouble(),
                productClass = stop.productClass.map { it }
            )
        }
        val protobufData = KrailNswStopList(nswStops = stopList)
        val adapter = KrailNswStopList.ADAPTER
        FileOutputStream("$cacheDirPath/NSW_STOPS.pb").use { output ->
            output.write(adapter.encode(protobufData))
        }
    }
}
