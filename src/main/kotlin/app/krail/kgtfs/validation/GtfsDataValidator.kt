package app.krail.kgtfs.validation

import app.krail.kgtfs.csv.CsvReader
import app.krail.kgtfs.model.readGtfsRoutes
import app.krail.kgtfs.model.readGtfsTrips
import app.krail.kgtfs.nsw.NswTransportModeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

/**
 * Validates GTFS data integrity by checking for:
 * - Orphaned stop references (stop_times.txt references stops not in stops.txt)
 * - Orphaned trip references
 * - Data consistency issues
 */
object GtfsDataValidator {

    data class ValidationReport(
        val totalStops: Int,
        val totalRoutes: Int,
        val totalTrips: Int,
        val totalStopTimes: Int,
        val orphanedStopIds: Set<String>,
        val orphanedTripIds: Set<String>,
        val routesWithOrphanedStops: Map<String, Set<String>>
    ) {
        fun printReport() {
            println("\n========================================")
            println("GTFS Data Validation Report")
            println("========================================")
            println("Total Stops: $totalStops")
            println("Total Routes: $totalRoutes")
            println("Total Trips: $totalTrips")
            println("Total Stop Times: $totalStopTimes")
            println()

            if (orphanedStopIds.isNotEmpty()) {
                println("⚠️  ORPHANED STOP IDs: ${orphanedStopIds.size}")
                println("These stop IDs are referenced in stop_times.txt but don't exist in stops.txt:")
                orphanedStopIds.take(20).forEach { println("  - $it") }
                if (orphanedStopIds.size > 20) {
                    println("  ... and ${orphanedStopIds.size - 20} more")
                }
                println()
            } else {
                println("✅ No orphaned stop IDs found")
            }

            if (orphanedTripIds.isNotEmpty()) {
                println("⚠️  ORPHANED TRIP IDs: ${orphanedTripIds.size}")
                println("These trip IDs are referenced in stop_times.txt but don't exist in trips.txt:")
                orphanedTripIds.take(10).forEach { println("  - $it") }
                if (orphanedTripIds.size > 10) {
                    println("  ... and ${orphanedTripIds.size - 10} more")
                }
                println()
            } else {
                println("✅ No orphaned trip IDs found")
            }

            if (routesWithOrphanedStops.isNotEmpty()) {
                println("⚠️  ROUTES WITH ORPHANED STOPS: ${routesWithOrphanedStops.size}")
                routesWithOrphanedStops.entries.take(10).forEach { (route, stopIds) ->
                    println("  Route $route: ${stopIds.size} orphaned stops")
                    stopIds.take(5).forEach { println("    - $it") }
                }
                println()
            } else {
                println("✅ All route stops are valid")
            }

            println("========================================\n")
        }
    }

    suspend fun validateGtfsData(
        cacheDirectory: String,
        modeName: String,
        nswTransportModeType: NswTransportModeType
    ): ValidationReport = withContext(Dispatchers.IO) {
        println("[Validator] Validating GTFS data for $modeName...")

        // Read all data
        val stops = CsvReader.readGtfsStops(
            path = "$cacheDirectory/$modeName/stops.txt".toPath(),
            nswTransportModeType = nswTransportModeType
        )

        val routes = readGtfsRoutes(
            path = "$cacheDirectory/$modeName/routes.txt".toPath()
        )

        val trips = readGtfsTrips(
            path = "$cacheDirectory/$modeName/trips.txt".toPath()
        )

        val stopTimes = CsvReader.readGtfsStopTimes(
            path = "$cacheDirectory/$modeName/stop_times.txt".toPath(),
            nswTransportModeType = nswTransportModeType
        )

        // Build lookup sets
        val validStopIds = stops.map { it.stopId.id }.toSet()
        val validTripIds = trips.map { it.tripId }.toSet()
        val tripIdToRoute = trips.associate { it.tripId to it.routeId }
        val routeIdToShortName = routes.associate { it.routeId to it.routeShortName }

        // Find orphaned stop IDs
        val referencedStopIds = stopTimes.map { it.stopId }.toSet()
        val orphanedStopIds = referencedStopIds - validStopIds

        // Find orphaned trip IDs
        val referencedTripIds = stopTimes.map { it.tripId }.toSet()
        val orphanedTripIds = referencedTripIds - validTripIds

        // Find routes with orphaned stops
        val routesWithOrphanedStops = mutableMapOf<String, MutableSet<String>>()

        stopTimes.forEach { stopTime ->
            if (stopTime.stopId in orphanedStopIds) {
                val routeId = tripIdToRoute[stopTime.tripId]
                if (routeId != null) {
                    val routeShortName = routeIdToShortName[routeId] ?: routeId
                    routesWithOrphanedStops
                        .getOrPut(routeShortName) { mutableSetOf() }
                        .add(stopTime.stopId)
                }
            }
        }

        return@withContext ValidationReport(
            totalStops = stops.size,
            totalRoutes = routes.size,
            totalTrips = trips.size,
            totalStopTimes = stopTimes.size,
            orphanedStopIds = orphanedStopIds,
            orphanedTripIds = orphanedTripIds,
            routesWithOrphanedStops = routesWithOrphanedStops
        )
    }
}

