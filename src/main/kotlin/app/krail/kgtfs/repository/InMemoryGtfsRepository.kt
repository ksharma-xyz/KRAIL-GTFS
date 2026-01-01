package app.krail.kgtfs.repository

import app.krail.kgtfs.csv.CsvReader
import app.krail.kgtfs.model.*
import app.krail.kgtfs.nsw.NswTransportModeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

/**
 * In-memory implementation of GtfsRepository with lazy initialization.
 *
 * Data is loaded and indexed on first access, then cached for subsequent queries.
 * Thread-safe via mutex lock during initialization.
 */
class InMemoryGtfsRepository(
    private val cacheDirectory: String,
    private val modeName: String,
    private val nswTransportModeType: NswTransportModeType
) : GtfsRepository {

    // Lazy-loaded data caches
    private var stops: List<GtfsStop> = emptyList()
    private var routes: List<GtfsRoute> = emptyList()
    private var trips: List<GtfsTrip> = emptyList()
    private var stopTimes: List<GtfsStopTime> = emptyList()

    // Computed indices
    private var routeToStopsMap: Map<String, List<GtfsStop>> = emptyMap()
    private var routeAndDirectionToStopsMap: Map<RouteDirection, List<GtfsStop>> = emptyMap()

    // Initialization state
    private var isInitialized = false
    private val initMutex = Mutex()

    /**
     * Composite key for route + direction lookups.
     */
    private data class RouteDirection(val routeShortName: String, val directionId: Int)

    /**
     * Lazy initialization - loads and indexes all GTFS data.
     */
    private suspend fun ensureInitialized() {
        if (isInitialized) return

        initMutex.withLock {
            // Double-check after acquiring lock
            if (isInitialized) return

            withContext(Dispatchers.IO) {
                println("[$modeName] Initializing repository...")

                try {
                    // 1. Load all files
                    stops = CsvReader.readGtfsStops(
                        path = "$cacheDirectory/$modeName/stops.txt".toPath(),
                        nswTransportModeType = nswTransportModeType
                    )
                    if (stops.isEmpty()) {
                        throw IllegalStateException("No stops loaded from: $cacheDirectory/$modeName/stops.txt")
                    }

                    routes = readGtfsRoutes(
                        path = "$cacheDirectory/$modeName/routes.txt".toPath()
                    )
                    if (routes.isEmpty()) {
                        throw IllegalStateException("No routes loaded from: $cacheDirectory/$modeName/routes.txt")
                    }

                    trips = readGtfsTrips(
                        path = "$cacheDirectory/$modeName/trips.txt".toPath()
                    )
                    if (trips.isEmpty()) {
                        throw IllegalStateException("No trips loaded from: $cacheDirectory/$modeName/trips.txt")
                    }

                    stopTimes = CsvReader.readGtfsStopTimes(
                        path = "$cacheDirectory/$modeName/stop_times.txt".toPath(),
                        nswTransportModeType = nswTransportModeType
                    )
                    if (stopTimes.isEmpty()) {
                        throw IllegalStateException("No stop times loaded from: $cacheDirectory/$modeName/stop_times.txt")
                    }

                    // 2. Build indices
                    buildIndices()

                    isInitialized = true
                    println("[$modeName] ✓ Repository ready: ${routes.size} routes, ${stops.size} stops")

                } catch (e: IllegalStateException) {
                    println("[$modeName] ✗ Initialization failed: ${e.message}")
                    println("[$modeName] Run NswGtfsManager.fetch() to download GTFS data")
                    throw e
                } catch (e: Exception) {
                    println("[$modeName] ✗ Initialization failed: ${e.javaClass.simpleName} - ${e.message}")
                    throw e
                }
            }
        }
    }

    /**
     * Build lookup indices for efficient querying.
     */
    private fun buildIndices() {
        println("[$modeName] Building route-to-stops indices...")

        // Create lookup maps
        val stopById = stops.associateBy { it.stopId.id }
        val routeIdToShortName = routes.associate { it.routeId to it.routeShortName }
        val tripIdToRoute = trips.associate { it.tripId to it.routeId }
        val tripIdToDirection = trips.associate { it.tripId to it.directionId }

        // Index stop times by trip for efficient lookup
        val stopTimesByTrip = stopTimes.groupBy { it.tripId }

        // Build route -> stops map (all directions combined)
        val routeStopsMap = mutableMapOf<String, LinkedHashSet<GtfsStop>>()

        // Build route + direction -> stops map
        val routeDirectionStopsMap = mutableMapOf<RouteDirection, LinkedHashSet<GtfsStop>>()

        // Process each trip
        trips.forEach { trip ->
            val routeId = trip.routeId
            val routeShortName = routeIdToShortName[routeId] ?: routeId
            val directionId = trip.directionId

            // Get stops for this trip
            val stopsForTrip = stopTimesByTrip[trip.tripId]
                ?.sortedBy { it.stopSequence }
                ?.mapNotNull { stopById[it.stopId] }
                ?: emptyList()

            if (stopsForTrip.isEmpty()) return@forEach

            // Add to route-level map (all directions)
            routeStopsMap.getOrPut(routeShortName) { linkedSetOf() }
                .addAll(stopsForTrip)

            // Add to route+direction map (if direction is specified)
            if (directionId != null) {
                val key = RouteDirection(routeShortName, directionId)
                routeDirectionStopsMap.getOrPut(key) { linkedSetOf() }
                    .addAll(stopsForTrip)
            }
        }

        // Convert to immutable maps
        routeToStopsMap = routeStopsMap.mapValues { it.value.toList() }
        routeAndDirectionToStopsMap = routeDirectionStopsMap.mapValues { it.value.toList() }

        println("[$modeName] Indices built - ${routeToStopsMap.size} routes indexed")
    }

    override suspend fun getStopsByRoute(routeShortName: String): Result<List<GtfsStop>> {
        return try {
            ensureInitialized()
            val stops = routeToStopsMap[routeShortName]
            if (stops != null) {
                Result.success(stops)
            } else {
                Result.failure(Exception("Route '$routeShortName' not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStopsByRouteAndDirection(
        routeShortName: String,
        directionId: Int
    ): Result<List<GtfsStop>> {
        return try {
            ensureInitialized()
            val key = RouteDirection(routeShortName, directionId)
            val stops = routeAndDirectionToStopsMap[key]
            if (stops != null) {
                Result.success(stops)
            } else {
                Result.failure(Exception("Route '$routeShortName' with direction $directionId not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllRoutes(): Result<List<String>> {
        return try {
            ensureInitialized()
            Result.success(routeToStopsMap.keys.sorted())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getStats(): Result<RepositoryStats> {
        return try {
            ensureInitialized()
            Result.success(
                RepositoryStats(
                    totalRoutes = routeToStopsMap.size,
                    totalStops = stops.size,
                    totalTrips = trips.size,
                    totalStopTimes = stopTimes.size,
                    transportMode = nswTransportModeType.modeName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

