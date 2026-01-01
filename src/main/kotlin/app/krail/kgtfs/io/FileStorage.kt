package app.krail.kgtfs.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okio.IOException
import okio.Path
import java.io.File

object FileStorage {

    @OptIn(ExperimentalSerializationApi::class)
    val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }
    val json = Json { prettyPrint = false }
    suspend fun saveFile(fileName: String, data: ByteArray, directory: String) = withContext(Dispatchers.IO) {
        val dir = File(directory)
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw IOException("Failed to create directory: $directory")
            }
        } else if (!dir.canWrite()) {
            throw IOException("Directory is not writable: $directory")
        }

        val file = File(dir, fileName)
        file.writeBytes(data)
    }

    suspend inline fun <reified T> writeJsonToFile(
        data: T,
        path: Path,
        fileName: String,
        pretty: Boolean = false
    ) = withContext(Dispatchers.IO) {
        // Configure JSON serializer based on the 'pretty' flag
        val json = if (pretty) prettyJson else json

        // Create the formatted file name (adds "PRETTY" if pretty is true)
        val finalFileName =
            if (pretty) "${fileName}_PRETTY${app.krail.kgtfs.AppConstants.FileExtensions.JSON}" else "$fileName${app.krail.kgtfs.AppConstants.FileExtensions.JSON}"

        // Write the serialized JSON data to the specified file path
        File(path.toFile(), finalFileName).writeText(json.encodeToString(data))
    }
}
