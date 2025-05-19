package app.krail.kgtfs.nsw

import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.NswTransport.fetchAndProcessNswTransportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    ): List<StopJson> = withContext(Dispatchers.Default) {

        val gtfsStopMap = modes.associateWith { mode ->
            mode.fetchAndProcessNswTransportData(fetchFromNetwork = refresh)
        }.toSortedMap()

        println("Map Order: ${gtfsStopMap.keys}")

        createCommonGtfsStops(gtfsStopMap)
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
                    val isNewTrain = mode.productClass == 1
                    val isExistingTrain = existingStop.productClass.contains(1)

                    if (gtfsStop.name.contains("Coach Stop") || existingStop.name.contains("Coach Stop")) {
                        existingStop.productClass.add(NswTransportModeType.COACH.productClass)
                    } else {
                        existingStop.productClass.add(mode.productClass)
                    }

                    // Prefer the name from the stop with productClass 1 (train)
                    if (!isExistingTrain && isNewTrain) {
                        existingStop.name = gtfsStop.name
                    }
                } else {
                    if (gtfsStop.name.contains("Coach Stop")) {
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
}
