package app.krail.kgtfs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Reference: https://gtfs.org/reference/static/#stopstxt
 * Fields in stops.txt gtfs static data.
 */
object GtfsStopField {
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
}

/**
 * Reference: https://gtfs.org/documentation/schedule/reference/#stopstxt
 * Stops where vehicles pick up or drop off riders. Also defines stations and station entrances.
 */
@Serializable
data class GtfsStop(

    /**
     * Uniquely Identifies a location: stop/platform, station, entrance/exit, generic node or
     * boarding area.
     */
    @SerialName(GtfsStopField.STOP_ID) val stopId: StopId,

    /**
     * Short text or a number that identifies the location for riders. These codes are often used
     * in phone-based transit information systems or printed on signage to make it easier for riders
     * to get information for a particular location. The stop_code may be the same as [stopId] if
     * it is public facing. This field should be left empty for locations without a code presented to riders.
     */
    @SerialName(GtfsStopField.STOP_CODE) val stopCode: String? = null,

    /**
     * Name of the location. The stop_name should match the agency's rider-facing name for the location
     * as printed on a timetable, published online, or represented on signage
     */
    @SerialName(GtfsStopField.STOP_NAME) val name: String,

    /**
     * Description of the location that provides useful, quality information. Should not be a
     * duplicate of stop_name.
     */
    @SerialName(GtfsStopField.STOP_DESC) val description: String? = null,

    /**
     * Latitude of the location.
     */
    @SerialName(GtfsStopField.STOP_LAT) val latitude: Double? = null,

    /**
     * Longitude of the location.
     */
    @SerialName(GtfsStopField.STOP_LON) val longitude: Double? = null,

    /**
     * Identifies the fare zone for a stop. If this record represents a station or station entrance,
     * the zone_id is ignored.
     */
    @SerialName(GtfsStopField.ZONE_ID) val zoneId: String? = null,

    /**
     * URL of a web page about the location. This should be different from the agency.agency_url
     * and the routes.route_url field values.
     */
    @SerialName(GtfsStopField.STOP_URL) val url: String? = null,

    /**
     * Location type. Valid options are:
     *
     * 0 (or empty) - Stop (or Platform). A location where passengers board or disembark from a
     * transit vehicle. Is called a platform when defined within a parent_station.
     * 1 - Station. A physical structure or area that contains one or more platform.
     * 2 - Entrance/Exit. A location where passengers can enter or exit a station from the street.
     * If an entrance/exit belongs to multiple stations, it may be linked by pathways to both,
     * but the data provider must pick one of them as parent.
     * 3 - Generic Node. A location within a station, not matching any other location_type, that
     * may be used to link together pathways define in pathways.txt.
     * 4 - Boarding Area. A specific location on a platform, where passengers can board and/or
     * alight vehicles.
     */
    @SerialName(GtfsStopField.LOCATION_TYPE) val locationType: String? = null,

    /**
     * Defines hierarchy between the different locations defined in stops.txt. It contains the ID
     * of the parent location, as followed:
     *
     * - Stop/platform (location_type=0): the parent_station field contains the ID of a station.
     * - Station (location_type=1): this field must be empty.
     * - Entrance/exit (location_type=2) or generic node (location_type=3): the parent_station
     * field contains the ID of a station (location_type=1)
     * - Boarding Area (location_type=4): the parent_station field contains ID of a platform.
     *
     */
    @SerialName(GtfsStopField.PARENT_STATION) val parentStation: StopId? = null,

    /**
     * Timezone of the location. If the location has a parent station, it inherits the parent
     * station’s timezone instead of applying its own. Stations and parentless stops with empty
     * stop_timezone inherit the timezone specified by agency.agency_timezone. The times provided
     * in stop_times.txt are in the timezone specified by agency.agency_timezone, not stop_timezone.
     * This ensures that the time values in a trip always increase over the course of a trip,
     * regardless of which timezones the trip crosses.
     */
    @SerialName(GtfsStopField.STOP_TIMEZONE) val timezone: String? = null,

    /**
     * Indicates whether wheelchair boardings are possible from the location. Valid options are:
     *
     * For parentless stops:
     * 0 or empty - No accessibility information for the stop.
     * 1 - Some vehicles at this stop can be boarded by a rider in a wheelchair.
     * 2 - Wheelchair boarding is not possible at this stop.
     *
     */
    @SerialName(GtfsStopField.WHEELCHAIR_BOARDING) val wheelchairBoarding: Int? = null,
)

@JvmInline
@Serializable
value class StopId(val id: String) {
    override fun toString(): String = id
}
