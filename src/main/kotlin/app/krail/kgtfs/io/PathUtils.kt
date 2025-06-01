package app.krail.kgtfs.io

import okio.Path.Companion.toPath
import java.nio.file.Paths

val projectRoot = Paths.get("").toAbsolutePath().toString()
val cacheDirectory = "$projectRoot/cache"
val cacheDirPath = cacheDirectory.toPath()

