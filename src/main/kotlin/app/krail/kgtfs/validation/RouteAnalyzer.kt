package app.krail.kgtfs.validation

import app.krail.kgtfs.csv.CsvReader
import app.krail.kgtfs.model.*
import app.krail.kgtfs.nsw.NswTransportModeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

/**
 * Analyzes specific routes to understand why they have certain stops.
 * Useful for debugging route construction logic.
 */
object RouteAnalyzer {

    data class TripPattern(
        val tripId: String,
        val directionId: Int?,
        val headsign: String?,
        val stopCount: Int,
        val stopIds: List<String>,
        val stopNames: List<String>
    )

    data class RouteAnalysis(
        val routeNumber: String,
        val totalUniqueStops: Int,
        val tripPatterns: List<TripPattern>,
        val stopFrequency: Map<String, StopFrequencyInfo>
    )

    data class StopFrequencyInfo(
        val stopId: String,
        val stopName: String,
        val tripCount: Int,
        val appearancePercentage: Double,
        val directions: Set<Int>
    )

    /**
     * Analyze a specific route in detail.
     *
     * @param cacheDirectory Base cache directory
     * @param modeName Transport mode name (e.g., "Buses")
     * @param routeNumber Route number to analyze (e.g., "729")
     * @param nswTransportModeType Transport mode type
     */
    suspend fun analyzeRoute(
        cacheDirectory: String,
        modeName: String,
        routeNumber: String,
        nswTransportModeType: NswTransportModeType
    ): RouteAnalysis? = withContext(Dispatchers.IO) {
        println("\n========================================")
        println("Analyzing Route $routeNumber")
        println("========================================\n")

        // Load data
        val stops = CsvReader.readGtfsStops(
            path = "$cacheDirectory/$modeName/stops.txt".toPath(),
            nswTransportModeType = nswTransportModeType
        )
        val stopById = stops.associateBy { it.stopId.id }

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

        // Find route ID
        val route = routes.find { it.routeShortName == routeNumber }
        if (route == null) {
            println("❌ Route $routeNumber not found")
            return@withContext null
        }

        println("✓ Route found: ${route.routeLongName ?: route.routeShortName}")
        println()

        // Get all trips for this route
        val routeTrips = trips.filter { it.routeId == route.routeId }
        println("Total trips for route: ${routeTrips.size}")

        // Group by direction
        val tripsByDirection = routeTrips.groupBy { it.directionId }
        tripsByDirection.forEach { (direction, trips) ->
            println("  Direction ${direction ?: "unknown"}: ${trips.size} trips")
        }
        println()

        // Index stop times
        val stopTimesByTrip = stopTimes.groupBy { it.tripId }

        // Analyze each trip pattern
        val tripPatterns = mutableListOf<TripPattern>()
        val stopFrequency = mutableMapOf<String, MutableSet<String>>() // stopId -> tripIds
        val stopDirections = mutableMapOf<String, MutableSet<Int>>() // stopId -> directions

        routeTrips.forEach { trip ->
            val stopsForTrip = stopTimesByTrip[trip.tripId]
                ?.sortedBy { it.stopSequence }
                ?.mapNotNull { stopTime ->
                    val stop = stopById[stopTime.stopId]
                    if (stop != null) {
                        // Track frequency
                        stopFrequency.getOrPut(stopTime.stopId) { mutableSetOf() }.add(trip.tripId)
                        // Track directions
                        trip.directionId?.let { dir ->
                            stopDirections.getOrPut(stopTime.stopId) { mutableSetOf() }.add(dir)
                        }
                    }
                    stop
                }
                ?: emptyList()

            if (stopsForTrip.isNotEmpty()) {
                tripPatterns.add(
                    TripPattern(
                        tripId = trip.tripId,
                        directionId = trip.directionId,
                        headsign = trip.tripHeadsign,
                        stopCount = stopsForTrip.size,
                        stopIds = stopsForTrip.map { it.stopId.id },
                        stopNames = stopsForTrip.map { it.name }
                    )
                )
            }
        }

        // Build stop frequency info
        val stopFrequencyInfo = stopFrequency.map { (stopId, tripIds) ->
            val stop = stopById[stopId]
            stopId to StopFrequencyInfo(
                stopId = stopId,
                stopName = stop?.name ?: "Unknown",
                tripCount = tripIds.size,
                appearancePercentage = (tripIds.size.toDouble() / routeTrips.size) * 100,
                directions = stopDirections[stopId] ?: emptySet()
            )
        }.toMap()

        // Find unique trip patterns
        val uniquePatterns = tripPatterns.groupBy { it.stopIds }
        println("Unique trip patterns: ${uniquePatterns.size}")
        uniquePatterns.entries.take(10).forEach { (stopIds, patterns) ->
            val pattern = patterns.first()
            println("  Pattern: ${pattern.stopCount} stops, ${patterns.size} trips, direction ${pattern.directionId}")
            println("    Headsign: ${pattern.headsign}")
            println("    First 5 stops: ${pattern.stopNames.take(5).joinToString(" → ")}")
        }
        if (uniquePatterns.size > 10) {
            println("  ... and ${uniquePatterns.size - 10} more patterns")
        }
        println()

        val allUniqueStops = stopFrequency.keys
        println("Total unique stops across all trips: ${allUniqueStops.size}")
        println()

        return@withContext RouteAnalysis(
            routeNumber = routeNumber,
            totalUniqueStops = allUniqueStops.size,
            tripPatterns = tripPatterns,
            stopFrequency = stopFrequencyInfo
        )
    }

    /**
     * Print detailed analysis report.
     */
    fun printDetailedReport(analysis: RouteAnalysis) {
        println("========================================")
        println("Detailed Analysis: Route ${analysis.routeNumber}")
        println("========================================\n")

        println("📊 Summary:")
        println("  Total unique stops: ${analysis.totalUniqueStops}")
        println("  Total trip patterns: ${analysis.tripPatterns.size}")
        println()

        // Stop frequency analysis
        println("📍 Stop Frequency (stops appearing in ALL trips vs some trips):")
        val sortedByFrequency = analysis.stopFrequency.values.sortedByDescending { it.tripCount }

        val appearsInAllTrips = sortedByFrequency.filter { it.appearancePercentage == 100.0 }
        val appearsInMostTrips = sortedByFrequency.filter { it.appearancePercentage >= 50.0 && it.appearancePercentage < 100.0 }
        val appearsInSomeTrips = sortedByFrequency.filter { it.appearancePercentage < 50.0 }

        println("\n  ✓ Stops in ALL trips (${appearsInAllTrips.size} stops):")
        appearsInAllTrips.take(10).forEach { stop ->
            println("    - ${stop.stopName} (${stop.stopId}) [${stop.tripCount} trips, ${stop.directions.joinToString("/")}]")
        }
        if (appearsInAllTrips.size > 10) {
            println("    ... and ${appearsInAllTrips.size - 10} more")
        }

        println("\n  ⚠️  Stops in MOST trips (>50%, ${appearsInMostTrips.size} stops):")
        appearsInMostTrips.take(10).forEach { stop ->
            println("    - ${stop.stopName} (${stop.stopId}) [${stop.tripCount} trips, ${String.format("%.1f", stop.appearancePercentage)}%, dirs: ${stop.directions.joinToString("/")}]")
        }
        if (appearsInMostTrips.size > 10) {
            println("    ... and ${appearsInMostTrips.size - 10} more")
        }

        println("\n  ℹ️  Stops in SOME trips (<50%, ${appearsInSomeTrips.size} stops):")
        println("    These might be:")
        println("      - Express/limited-stop variants")
        println("      - Peak-hour only stops")
        println("      - Different route endings")
        appearsInSomeTrips.take(10).forEach { stop ->
            println("    - ${stop.stopName} (${stop.stopId}) [${stop.tripCount} trips, ${String.format("%.1f", stop.appearancePercentage)}%]")
        }
        if (appearsInSomeTrips.size > 10) {
            println("    ... and ${appearsInSomeTrips.size - 10} more")
        }
        println()

        // Direction analysis
        val byDirection = analysis.tripPatterns.groupBy { it.directionId }
        println("🔀 Direction Analysis:")
        byDirection.forEach { (direction, patterns) ->
            val uniqueStops = patterns.flatMap { it.stopIds }.toSet()
            println("  Direction ${direction ?: "unknown"}: ${patterns.size} trips, ${uniqueStops.size} unique stops")

            // Most common headsign
            val headsigns = patterns.mapNotNull { it.headsign }.groupingBy { it }.eachCount()
            val topHeadsign = headsigns.maxByOrNull { it.value }
            if (topHeadsign != null) {
                println("    Main destination: ${topHeadsign.key} (${topHeadsign.value} trips)")
            }
        }
        println()

        println("========================================\n")
    }

    /**
     * Export stop frequency to CSV for analysis.
     */
    suspend fun exportStopFrequencyToCsv(
        analysis: RouteAnalysis,
        outputPath: String
    ) = withContext(Dispatchers.IO) {
        val csvContent = buildString {
            appendLine("stop_id,stop_name,trip_count,percentage,directions")
            analysis.stopFrequency.values
                .sortedByDescending { it.tripCount }
                .forEach { stop ->
                    appendLine("${stop.stopId},\"${stop.stopName}\",${stop.tripCount},${String.format("%.2f", stop.appearancePercentage)},\"${stop.directions.joinToString("/")}\"")
                }
        }

        java.io.File(outputPath).writeText(csvContent)
        println("✓ Exported stop frequency to: $outputPath")
    }
}

