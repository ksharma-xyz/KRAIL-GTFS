package app.krail.kgtfs.io

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.io.NswStopsProtoIO.readProtoFile
import app.krail.kgtfs.io.NswStopsProtoIO.writeProtoFile
import app.krail.kgtfs.model.StopJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NswStopsJsonIO {

    suspend fun writeStopData(result: List<StopJson>): Unit = withContext(Dispatchers.IO) {
        // Write as pretty JSON
        writeJsonToFile(
            data = result,
            path = AppConstants.CACHE_DIR_PATH,
            fileName = AppConstants.OutputFiles.NSW_STOPS,
            pretty = true,
        )

        // Write as compact JSON
        writeJsonToFile(
            data = result,
            path = AppConstants.CACHE_DIR_PATH,
            fileName = AppConstants.OutputFiles.NSW_STOPS,
            pretty = false,
        )

        // Write as Protobuf
        writeProtoFile(data = result, filePath = "${AppConstants.CACHE_DIR_PATH}/${AppConstants.OutputFiles.NSW_STOPS_PB}")

        // Only for testing purposes
        readProtoFile("${AppConstants.CACHE_DIR_PATH}/${AppConstants.OutputFiles.NSW_STOPS_PB}")
    }
}