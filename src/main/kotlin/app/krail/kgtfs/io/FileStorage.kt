package app.krail.kgtfs.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File

object FileStorage {

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

    const val ZIP_EXTENSION = ".zip"
    const val TXT_EXTENSION = ".txt"
    const val JSON_EXTENSION = ".json"
}
