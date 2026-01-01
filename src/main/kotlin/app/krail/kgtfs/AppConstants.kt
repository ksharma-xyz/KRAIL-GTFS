package app.krail.kgtfs

import okio.Path.Companion.toPath
import java.nio.file.Paths

/**
 * Application-wide constants.
 * Centralizes configuration to avoid hardcoded values across the codebase.
 */
object AppConstants {

    /**
     * Project root directory (absolute path).
     */
    val PROJECT_ROOT: String = Paths.get("").toAbsolutePath().toString()

    /**
     * Cache directory name (relative).
     */
    const val CACHE_DIR_NAME = "cache"

    /**
     * Full cache directory path.
     */
    val CACHE_DIRECTORY = "$PROJECT_ROOT/$CACHE_DIR_NAME"

    /**
     * Cache directory as Okio Path.
     */
    val CACHE_DIR_PATH = CACHE_DIRECTORY.toPath()

    /**
     * File extensions.
     */
    object FileExtensions {
        const val TXT = ".txt"
        const val JSON = ".json"
        const val PROTOBUF = ".pb"
        const val ZIP = ".zip"
    }

    /**
     * Output file names.
     */
    object OutputFiles {
        const val NSW_STOPS = "NSW_STOPS"
        const val NSW_STOPS_PB = "NSW_STOPS.pb"
        const val NSW_PARK_RIDE = "NSW_PARKRIDE"

        // Route files
        const val NSW_BUSES_ROUTES = "NSW_BUSES_ROUTES"
        const val NSW_BUSES_ROUTES_PB = "NSW_BUSES_ROUTES.pb"
    }
}
