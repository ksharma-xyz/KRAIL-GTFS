package app.krail.kgtfs.io

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.io.NswStopsProtoIO.readProtoFile
import app.krail.kgtfs.io.NswStopsProtoIO.writeProtoFile
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.network.cacheDirPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NswStopsJsonIO {

    suspend fun writeStopData(result: List<StopJson>) = withContext(Dispatchers.IO) {
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

        // Write as Protobuf
        writeProtoFile(data = result, filePath = "$cacheDirPath/NSW_STOPS.pb")

        // Only for testing purposes
        readProtoFile("$cacheDirPath/NSW_STOPS.pb")
    }
}