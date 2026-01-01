package app.krail.kgtfs.model

import app.krail.kgtfs.csv.CsvReader
import app.krail.kgtfs.nsw.NswTransportModeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toPath

data class GtfsRoute(
    val routeId: String,
    val routeShortName: String,
    val routeLongName: String? = null,
    val routeDesc: String? = null, // Often contains Agency or Network info
    val routeType: String? = null
)

data class GtfsTrip(
    val tripId: String,
    val routeId: String,
    val directionId: Int? = null,
    val tripHeadsign: String? = null,
    val routeDirection: String? = null // The specific description user mentioned (last column)
)

suspend fun readGtfsRoutes(path: Path): List<GtfsRoute> = withContext(Dispatchers.IO) {
    try {
        val routes = CsvReader.readCsvFile(path) { row ->
            val routeId = row["route_id"] ?: return@readCsvFile null
            val routeShortName = row["route_short_name"] ?: routeId
            GtfsRoute(
                routeId = routeId,
                routeShortName = routeShortName,
                routeLongName = row["route_long_name"],
                routeDesc = row["route_desc"],
                routeType = row["route_type"]
            )
        }.filterNotNull()
        routes
    } catch (e: java.io.FileNotFoundException) {
        println("[RouteReader] ERROR: File not found: $path")
        emptyList()
    } catch (e: Exception) {
        println("[RouteReader] ERROR: Failed to parse routes - ${e.message}")
        emptyList()
    }
}

suspend fun readGtfsTrips(path: Path): List<GtfsTrip> = withContext(Dispatchers.IO) {
    try {
        val trips = CsvReader.readCsvFile(path) { row ->
            val tripId = row["trip_id"]
            val routeId = row["route_id"]

            if (tripId == null || routeId == null) {
                return@readCsvFile null
            }

            // Try to find the "route_direction" which user says is the last column.
            // In standard GTFS this might be mapped differently, but we'll look for "route_direction" header if it exists,
            // or fall back to trip_headsign.
            val routeDirection = row["route_direction"]

            GtfsTrip(
                tripId = tripId,
                routeId = routeId,
                directionId = row["direction_id"]?.toIntOrNull(),
                tripHeadsign = row["trip_headsign"],
                routeDirection = routeDirection
            )
        }.filterNotNull()
        trips
    } catch (e: java.io.FileNotFoundException) {
        println("[TripReader] ERROR: File not found: $path")
        emptyList()
    } catch (e: Exception) {
        println("[TripReader] ERROR: Failed to parse trips - ${e.message}")
        emptyList()
    }
}

object RouteToStopsBuilder {

    suspend fun buildRouteToStopsMap(
        cacheDirectory: String,
        modeName: String,
        nswTransportModeType: NswTransportModeType
    ): Map<String, List<GtfsStop>> = withContext(Dispatchers.IO) {
        println("Building route-to-stops map for $modeName...")

        // 1. Read all required files
        println("Reading stops...")
        val stops = CsvReader.readGtfsStops(
            path = "$cacheDirectory/$modeName/stops.txt".toPath(),
            nswTransportModeType = nswTransportModeType
        )

        println("Reading routes...")
        val routes = readGtfsRoutes(
            path = "$cacheDirectory/$modeName/routes.txt".toPath()
        )

        println("Reading trips...")
        val trips = readGtfsTrips(
            path = "$cacheDirectory/$modeName/trips.txt".toPath()
        )

        println("Reading stop times...")
        val stopTimes = CsvReader.readGtfsStopTimes(
            path = "$cacheDirectory/$modeName/stop_times.txt".toPath(),
            nswTransportModeType = nswTransportModeType
        )

        println("Files loaded - Stops: ${stops.size}, Routes: ${routes.size}, Trips: ${trips.size}, StopTimes: ${stopTimes.size}")

        // 2. Create lookup maps
        val stopById = stops.associateBy { it.stopId.id }
        val routeIdToShortName = routes.associate { it.routeId to it.routeShortName }

        // 3. Group trips by route_id
        val tripsByRoute = trips.groupBy { it.routeId }

        // 4. Index stop times by trip_id for efficient lookup
        val stopTimesByTrip = stopTimes.groupBy { it.tripId }

        // 5. For each route, aggregate stops across all trips
        val routeToStopsMap = mutableMapOf<String, List<GtfsStop>>()

        tripsByRoute.forEach { (routeId, tripsForRoute) ->
            val routeShortName = routeIdToShortName[routeId] ?: routeId
            val allStopsForRoute = linkedSetOf<GtfsStop>() // preserves order, removes duplicates

            tripsForRoute.forEach { trip ->
                val stopsForTrip = stopTimesByTrip[trip.tripId]
                    ?.sortedBy { it.stopSequence }
                    ?.mapNotNull { stopById[it.stopId] }
                    ?: emptyList()

                allStopsForRoute.addAll(stopsForTrip)
            }

            if (allStopsForRoute.isNotEmpty()) {
                routeToStopsMap[routeShortName] = allStopsForRoute.toList()
            }
        }

        println("Route-to-stops map built: ${routeToStopsMap.size} routes")
        return@withContext routeToStopsMap
    }

    fun printRouteStops(routeNumber: String, stops: List<GtfsStop>) {
        println("\n=== Route $routeNumber ===")
        println("Total stops: ${stops.size}")
        println("\nFirst 10 stops:")
        stops.take(10).forEachIndexed { index, stop ->
            println("  ${index + 1}. ${stop.name} (${stop.stopId.id})")
        }
        if (stops.size > 10) {
            println("  ... and ${stops.size - 10} more stops")
        }
    }
}
