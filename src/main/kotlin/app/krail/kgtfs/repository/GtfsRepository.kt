package app.krail.kgtfs.repository

import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.RouteVariant

/**
 * Repository interface for accessing GTFS route and stop data.
 *
 * This abstraction allows for different implementations (in-memory, database, etc.)
 * and provides a clean API for querying route-to-stops relationships.
 */
interface GtfsRepository {

    /**
     * Get all stops serviced by a given route number.
     *
     * @param routeShortName The route number (e.g., "303", "M50")
     * @return Result containing list of stops in sequence, or error
     */
    suspend fun getStopsByRoute(routeShortName: String): Result<List<GtfsStop>>

    /**
     * Get stops serviced by a route in a specific direction.
     *
     * @param routeShortName The route number
     * @param directionId Direction identifier (typically 0 or 1)
     * @return Result containing list of stops in sequence for that direction, or error
     */
    suspend fun getStopsByRouteAndDirection(
        routeShortName: String,
        directionId: Int
    ): Result<List<GtfsStop>>

    /**
     * Get all available route numbers.
     *
     * @return Result containing list of all route short names, or error
     */
    suspend fun getAllRoutes(): Result<List<String>>

    /**
     * Get summary statistics about the loaded GTFS data.
     *
     * @return Result containing repository stats, or error
     */
    suspend fun getStats(): Result<RepositoryStats>

    /**
     * Get structured route data for the new user flow.
     * Grouped by route_short_name -> list of variants (route_id) -> list of directions.
     */
    suspend fun getStructuredRouteData(): Result<Map<String, List<RouteVariant>>>
}

/**
 * Statistics about the loaded GTFS repository data.
 */
data class RepositoryStats(
    val totalRoutes: Int,
    val totalStops: Int,
    val totalTrips: Int,
    val totalStopTimes: Int,
    val transportMode: String
)
