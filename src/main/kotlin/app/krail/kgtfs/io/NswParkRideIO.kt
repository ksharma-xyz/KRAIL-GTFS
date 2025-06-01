package app.krail.kgtfs.io

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

const val NSW_PARK_RIDE_DIR = "nswstops/parkride"

/**
 * Writes the park & ride data to JSON files.
 *
 * This function filters the provided list of stops to only include those that have a corresponding
 * park & ride mapping, and then writes the filtered mappings to two JSON files: one pretty-printed
 * and one compact.
 *
 * @param result The list of [StopJson] objects to filter and write.
 *
 * NOTE: This function assumes that `stopIdParkRideMappings` is a pre-populated list of mappings
 * This function can be used to write park & ride data after fetching it from the API.
 * This is a static way to write the park & ride data based on the provided stop list.
 * for fresh data from api everytime use [processParkRideData] instead.
 */
suspend fun writeParkRideData(result: List<StopJson>) = withContext(Dispatchers.IO) {
    // Extract all stop IDs from the result list and put them in a Set for fast lookup
    val stopIds = result.map { stop -> stop.id }.toSet()

    // Filter the park & ride mappings to only include those whose stopId is present in the result set
    val filteredMappings = stopIdParkRideMappings.filter { it.stopId in stopIds }

    writeJsonToFile(
        data = filteredMappings,
        path = cacheDirPath,
        fileName = "NSW_PARKRIDE_PRETTY",
        pretty = true,
    )
    writeJsonToFile(
        data = filteredMappings,
        path = cacheDirPath,
        fileName = "NSW_PARKRIDE",
        pretty = false,
    )
}
