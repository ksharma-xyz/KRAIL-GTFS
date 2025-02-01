package app.krail.kgtfs.io

import okio.FileSystem
import okio.Path

val fileSystem = FileSystem.SYSTEM

internal fun checkIfPathExists(destDir: Path) {
    if (fileSystem.exists(destDir)) {
        println("Path Exists: $destDir")
    } else {
        println("Path does not exist: $destDir")
    }
}

internal fun listPathsUnderDirectory(directory: Path): List<Path> {
    return runCatching {
        fileSystem.list(directory)
    }.getOrElse { error ->
        println("Failed to list paths under directory: $directory. Error: ${error.message}")
        emptyList()
    }
}

internal fun String.dropExtension(): String {
    val lastDot = this.lastIndexOf('.')
    return if (lastDot > 0) this.substring(0, lastDot) else this
}
