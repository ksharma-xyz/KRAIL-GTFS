package app.krail.kgtfs.model

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.csv.CsvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path

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
            val routeId = row[AppConstants.GtfsFields.ROUTE_ID] ?: return@readCsvFile null
            val routeShortName = row[AppConstants.GtfsFields.ROUTE_SHORT_NAME] ?: routeId
            GtfsRoute(
                routeId = routeId,
                routeShortName = routeShortName,
                routeLongName = row[AppConstants.GtfsFields.ROUTE_LONG_NAME],
                routeDesc = row[AppConstants.GtfsFields.ROUTE_DESC],
                routeType = row[AppConstants.GtfsFields.ROUTE_TYPE]
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
            val tripId = row[AppConstants.GtfsFields.TRIP_ID]
            val routeId = row[AppConstants.GtfsFields.ROUTE_ID]

            if (tripId == null || routeId == null) {
                return@readCsvFile null
            }

            // Try to find the "route_direction" which user says is the last column.
            // In standard GTFS this might be mapped differently, but we'll look for "route_direction" header if it exists,
            // or fall back to trip_headsign.
            val routeDirection = row[AppConstants.GtfsFields.ROUTE_DIRECTION]

            GtfsTrip(
                tripId = tripId,
                routeId = routeId,
                directionId = row[AppConstants.GtfsFields.DIRECTION_ID]?.toIntOrNull(),
                tripHeadsign = row[AppConstants.GtfsFields.TRIP_HEADSIGN],
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
