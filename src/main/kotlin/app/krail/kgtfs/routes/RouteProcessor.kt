package app.krail.kgtfs.routes

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.io.RouteStopsJsonIO.writeRouteStopsDataByMode
import app.krail.kgtfs.nsw.NswTransportModeType
import app.krail.kgtfs.repository.GtfsRepositoryFactory

/**
 * Processes and exports route data to JSON/Protobuf formats.
 */
object RouteProcessor {

    /**
     * Process routes for a given transport mode and export to files.
     *
     * @param transportMode Transport mode to process
     * @param exportToJson Whether to export to JSON (default: true)
     * @param exportToProtobuf Whether to export to Protobuf (default: true)
     */
    suspend fun processAndExport(
        transportMode: NswTransportModeType,
        exportToJson: Boolean = true,
        exportToProtobuf: Boolean = true
    ) {
        try {
            // Create repository
            val repository = GtfsRepositoryFactory.create(
                cacheDirectory = AppConstants.CACHE_DIRECTORY,
                nswTransportModeType = transportMode
            )

            // Validate repository has data
            repository.getStats().fold(
                onSuccess = { stats ->
                    if (stats.totalRoutes == 0 || stats.totalStops == 0) {
                        println("[RouteProcessor] ERROR: No data found for ${transportMode.modeName}")
                        return
                    }
                    println("[RouteProcessor] Processing ${stats.totalRoutes} routes for ${transportMode.modeName}")
                },
                onFailure = { e ->
                    println("[RouteProcessor] ERROR: Failed to get stats - ${e.message}")
                    return
                }
            )

            // Export (JSON and/or Protobuf)
            if (exportToJson || exportToProtobuf) {
                val filePrefix = "NSW_${transportMode.modeName.uppercase()}_ROUTES"
                writeRouteStopsDataByMode(
                    repository = repository,
                    transportMode = transportMode.modeName,
                    filePrefix = filePrefix,
                    exportProtobuf = exportToProtobuf
                )
            }

        } catch (e: IllegalStateException) {
            println("[RouteProcessor] ERROR: ${e.message}")
            println("[RouteProcessor] Run NswGtfsManager.fetch() to download GTFS data")
        } catch (e: Exception) {
            println("[RouteProcessor] ERROR: ${e.javaClass.simpleName} - ${e.message}")
            throw e
        }
    }
}
