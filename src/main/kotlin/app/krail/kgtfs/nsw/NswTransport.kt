package app.krail.kgtfs.nsw

import app.krail.kgtfs.csv.CsvReader.readGtfsStops
import app.krail.kgtfs.io.FileStorage.TXT_EXTENSION
import app.krail.kgtfs.io.FileStorage.ZIP_EXTENSION
import app.krail.kgtfs.io.FileStorage.saveFile
import app.krail.kgtfs.io.ZipFileManager.unzip
import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.network.NswGtfsService.fetchBusesGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchLightRailCbdAndSoutheastGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchLightRailGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchLightRailInnerwestGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchLightRailNewcastleGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchLightRailParramattaGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchNswTrainsGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchSydneyFerriesGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchSydneyMetroGtfs
import app.krail.kgtfs.network.NswGtfsService.fetchSydneyTrainsGtfs
import app.krail.kgtfs.network.cacheDirectory
import io.ktor.client.statement.*
import okio.Path.Companion.toPath

object NswTransport {

    /**
     * Fetches and processes GTFS data for the given [NswTransportModeType]
     * 1. will fetch gtfs data from api
     * 2. will save the data to cache directory
     * 3. will unzip the data
     * 4. will read the stops from the unzipped data
     *
     * @return the list of [GtfsStop] objects
     */
    suspend fun NswTransportModeType.fetchAndProcessNswTransportData(
        fetchFromNetwork: Boolean = true,
    ): List<GtfsStop> {
        if (fetchFromNetwork) {
            println("$modeName: Downloading GTFS Data")

            // Fetch the data from the API
            val result = fetchNswTransportModeGtfsData(nswTransportModeType = this)


            val response = result.getOrElse { error ->
                println("ERROR: Failed to fetch $modeName GTFS Data: $error")
                return emptyList()
            }

            // Save the data to cache directory
            val data = response.readRawBytes()
            saveFile(
                fileName = "$modeName$ZIP_EXTENSION",
                data = data,
                directory = cacheDirectory,
            )
            println("$modeName: Download Complete")
        }

        // Unzip the data
        unzip(
            zipPath = "$cacheDirectory/$modeName$ZIP_EXTENSION".toPath(),
            destinationPath = "$cacheDirectory/$modeName".toPath()
        )

        // Read the stops from the unzipped data from TXT file.
        // Convert the data to [GtfsStop] list
        val gtfsStops = readGtfsStops(
            path = "$cacheDirectory/$modeName/stops$TXT_EXTENSION".toPath(),
            nswTransportModeType = this,
        )

        // Read the stop times from the unzipped data from TXT file.
        // Convert the data to [GtfsStopTime] list.
        // TODO - This has a issue, stop_times do not have all stopIds
        /*val gtfsStopTimes = readGtfsStopTimes(
            path = "$cacheDirectory/$modeName/stop_times$TXT_EXTENSION".toPath(),
            nswTransportModeType = this,
        )*/

        return gtfsStops
    }

    private suspend fun fetchNswTransportModeGtfsData(nswTransportModeType: NswTransportModeType): Result<HttpResponse> {
        return when (nswTransportModeType) {
            NswTransportModeType.SYDNEY_TRAINS -> fetchSydneyTrainsGtfs()
            NswTransportModeType.BUSES -> fetchBusesGtfs()
            NswTransportModeType.SYDNEY_METRO -> fetchSydneyMetroGtfs()
            NswTransportModeType.LIGHT_RAIL -> fetchLightRailGtfs()

            NswTransportModeType.NSW_TRAINS -> fetchNswTrainsGtfs()
            NswTransportModeType.SYDNEY_FERRY -> fetchSydneyFerriesGtfs()
            NswTransportModeType.COACH -> {
                // This should never happen. No API for Coach Stops
                throw IllegalStateException("COACH mode is not supported")
            }

            NswTransportModeType.LIGHT_RAIL_PARRAMATTA -> fetchLightRailParramattaGtfs()
            NswTransportModeType.LIGHT_RAIL_INNERWEST -> fetchLightRailInnerwestGtfs()
            NswTransportModeType.LIGHT_RAIL_NEWCASTLE -> fetchLightRailNewcastleGtfs()
            NswTransportModeType.LIGHT_RAIL_CBD_AND_SOUTHEAST -> fetchLightRailCbdAndSoutheastGtfs()
        }
    }
}
