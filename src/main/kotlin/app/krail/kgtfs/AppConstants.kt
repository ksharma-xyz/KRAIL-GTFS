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

    /**
     * GTFS input file names (without extension).
     */
    object GtfsFiles {
        const val AGENCY = "agency"
        const val CALENDAR = "calendar"
        const val CALENDAR_DATES = "calendar_dates"
        const val NOTES = "notes"
        const val ROUTES = "routes"
        const val SHAPES = "shapes"
        const val STOP_TIMES = "stop_times"
        const val STOPS = "stops"
        const val TRIPS = "trips"
    }

    /**
     * GTFS CSV Header Fields.
     */
    object GtfsFields {
        // Routes
        const val ROUTE_ID = "route_id"
        const val ROUTE_SHORT_NAME = "route_short_name"
        const val ROUTE_LONG_NAME = "route_long_name"
        const val ROUTE_DESC = "route_desc"
        const val ROUTE_TYPE = "route_type"

        // Trips
        const val TRIP_ID = "trip_id"
        const val DIRECTION_ID = "direction_id"
        const val TRIP_HEADSIGN = "trip_headsign"
        const val ROUTE_DIRECTION = "route_direction" // Custom field in NSW GTFS

        // Stops
        const val STOP_ID = "stop_id"
        const val STOP_CODE = "stop_code"
        const val STOP_NAME = "stop_name"
        const val STOP_DESC = "stop_desc"
        const val STOP_LAT = "stop_lat"
        const val STOP_LON = "stop_lon"
        const val ZONE_ID = "zone_id"
        const val STOP_URL = "stop_url"
        const val LOCATION_TYPE = "location_type"
        const val PARENT_STATION = "parent_station"
        const val STOP_TIMEZONE = "stop_timezone"
        const val WHEELCHAIR_BOARDING = "wheelchair_boarding"

        // Stop Times
        const val ARRIVAL_TIME = "arrival_time"
        const val DEPARTURE_TIME = "departure_time"
        const val STOP_SEQUENCE = "stop_sequence"
        const val STOP_HEADSIGN = "stop_headsign"
        const val PICKUP_TYPE = "pickup_type"
        const val DROP_OFF_TYPE = "drop_off_type"
        const val TIMEPOINT = "timepoint"
        const val STOP_NOTE = "stop_note"
        const val SHAPE_DIST_TRAVELED = "shape_dist_traveled"
    }
}
