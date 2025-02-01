package app.krail.kgtfs.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip

object ZipFileManager {

    suspend fun unzip(zipPath: Path, destinationPath: Path?): Path = withContext(Dispatchers.IO) {
        //      println("Unpacking Zip: $zipPath")

        val destDir: Path = destinationPath ?: zipPath.parent?.resolve(zipPath.name.dropExtension())
        ?: throw IllegalArgumentException("Invalid path: $zipPath")

//        println("Zip Unpack Destination: $destDir")

        fileSystem.createDirectories(destDir)

        // region Debugging Code
        // checkIfPathExists(destDir)
        //val paths = listPathsUnderDirectory(zipPath.parent!!)
        /*
                println("Paths under directory: $destDir")
                println("\t" + paths.joinToString("\n"))
        */
        // endregion Debugging Code end

        unpackZipToDirectory(zipPath, destDir)

        return@withContext destDir
    }

    /**
     * Unpacks the contents of a zip file to a specified destination directory.
     *
     * @param zipFile The path to the zip file to be unpacked.
     * @param destDir The path to the destination directory where the contents will be unpacked.
     */
    private suspend fun unpackZipToDirectory(zipFile: Path, destDir: Path) =
        withContext(Dispatchers.IO) {
            // Open the zip file as a file system
            val zipFileSystem = fileSystem.openZip(zipFile)

            // List all regular files in the zip file recursively
            val paths = zipFileSystem.listRecursively("/".toPath())
                .filter { zipFileSystem.metadata(it).isRegularFile }.toList()
            //println("Unzipping ${paths.size} files from $zipFile to $destDir")

            // Iterate over each file path in the zip file
            paths.forEach { zipFilePath ->
//                println("Unzipping: $zipFilePath")

                // Open the source file from the zip file system
                zipFileSystem.source(zipFilePath).buffer().use { source ->
                    // Determine the relative file path and resolve it to the destination directory
                    val relativeFilePath = zipFilePath.toString().trimStart('/')

                    // The resolve method in the context of file paths is used to combine two paths.
                    // It appends the given path to the current path, effectively creating a new path
                    // that represents a sub-path or a file within the current path.
                    val fileToWrite = destDir.resolve(relativeFilePath)
                    ////println("Writing to: $fileToWrite")

                    // Create parent directories for the destination file
                    fileToWrite.createParentDirectories()

                    // Write the contents of the source file to the destination file
                    fileSystem.sink(fileToWrite).buffer().use { sink ->
                        val bytes = sink.writeAll(source)
                        // println("Unzipped: $relativeFilePath to $fileToWrite; $bytes bytes written")
                    }
                }
            }
        }


    private fun Path.createParentDirectories() = parent?.let { parent ->
        try {
            fileSystem.createDirectories(parent)
            //println("Directories created successfully: $parent")
        } catch (e: Exception) {
            println("Failed to create directories: $parent. Error: ${e.message}")
            throw e
        }
    }
}