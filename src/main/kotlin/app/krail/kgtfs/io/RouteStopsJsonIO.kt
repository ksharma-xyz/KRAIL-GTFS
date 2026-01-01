package app.krail.kgtfs.io

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.io.RouteStopsProtoIO.writeProtoFile
import app.krail.kgtfs.model.MinimalRouteStopsJson
import app.krail.kgtfs.repository.GtfsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Exports route-to-stops data in MINIMAL format (JSON + Protobuf).
 * Only exports route numbers and ordered stop IDs.
 * Mobile app already has stop details in local DB.
 */
object RouteStopsJsonIO {

    /**
     * Export minimal route-to-stops data to JSON and optionally Protobuf.
     *
     * @param repository The GTFS repository to export from
     * @param transportMode Name of transport mode (e.g., "Buses")
     * @param filePrefix Filename prefix (e.g., "NSW_BUSES_ROUTES")
     * @param exportProtobuf Whether to also export .pb file
     */
    suspend fun writeRouteStopsDataByMode(
        repository: GtfsRepository,
        transportMode: String,
        filePrefix: String,
        exportProtobuf: Boolean = true
    ): Unit = withContext(Dispatchers.IO) {

        val statsResult = repository.getStats().getOrNull()
        val allRoutesResult = repository.getAllRoutes().getOrNull()

        if (statsResult == null || allRoutesResult == null) {
            println("[$transportMode] ERROR: Failed to fetch repository data")
            return@withContext
        }

        // Build minimal map: route_number -> [stop_ids]
        val routeToStopIds = mutableMapOf<String, List<String>>()

        allRoutesResult.forEach { routeNumber ->
            repository.getStopsByRoute(routeNumber).fold(
                onSuccess = { stops ->
                    routeToStopIds[routeNumber] = stops.map { it.stopId.id }
                },
                onFailure = { /* Skip failed routes silently */ }
            )
        }

        // Create minimal JSON structure
        val minimalJson = MinimalRouteStopsJson(
            transportMode = statsResult.transportMode,
            totalRoutes = routeToStopIds.size,
            generatedAt = Instant.now().toString(),
            routes = routeToStopIds.toSortedMap()
        )

        // Write pretty JSON
        writeJsonToFile(
            data = minimalJson,
            path = AppConstants.CACHE_DIR_PATH,
            fileName = filePrefix,
            pretty = true
        )

        // Write compact JSON
        writeJsonToFile(
            data = minimalJson,
            path = AppConstants.CACHE_DIR_PATH,
            fileName = filePrefix,
            pretty = false
        )

        val compactFile = java.io.File(AppConstants.CACHE_DIR_PATH.toFile(), "${filePrefix}.json")
        println("[$transportMode] ✓ Exported ${routeToStopIds.size} routes (${compactFile.length() / 1024} KB)")

        // Export to Protobuf
        if (exportProtobuf) {
            val pbFilePath = "${AppConstants.CACHE_DIRECTORY}/${filePrefix}${AppConstants.FileExtensions.PROTOBUF}"
            writeProtoFile(minimalJson, pbFilePath)
            val pbFile = java.io.File(pbFilePath)
            println("[$transportMode] ✓ Protobuf: ${pbFile.name} (${pbFile.length() / 1024} KB)")
        }
    }
}

