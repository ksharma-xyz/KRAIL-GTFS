package app.krail.kgtfs.io

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

suspend fun writeParkRideData(result: List<StopJson>) = withContext(Dispatchers.IO) {
    val stopIds = result.map { stop -> stop.id }.toSet()
    val filteredMappings = stopIdParkRideMappings.filter { it.stopId in stopIds }

    writeJsonToFile(
        data = filteredMappings,
        path = "nswstops/parkride".toPath(),
        fileName = "NSW_PARKRIDE_PRETTY",
        pretty = true,
    )
    writeJsonToFile(
        data = filteredMappings,
        path = "nswstops/parkride".toPath(),
        fileName = "NSW_PARKRIDE",
        pretty = false,
    )
}