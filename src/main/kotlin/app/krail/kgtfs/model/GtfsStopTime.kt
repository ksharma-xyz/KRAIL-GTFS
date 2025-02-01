package app.krail.kgtfs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Reference: https://gtfs.org/documentation/schedule/reference/#stop_timestxt
 * Fields in stop_times.txt gtfs static data.
 */
object GtfsStopTimeField {
    const val TRIP_ID = "trip_id"
    const val ARRIVAL_TIME = "arrival_time"
    const val DEPARTURE_TIME = "departure_time"
    const val STOP_ID = "stop_id"
    const val STOP_SEQUENCE = "stop_sequence"
    const val STOP_HEADSIGN = "stop_headsign"
    const val PICKUP_TYPE = "pickup_type"
    const val DROP_OFF_TYPE = "drop_off_type"
    const val TIMEPOINT = "timepoint"
    const val STOP_NOTE = "stop_note"
    const val SHAPE_DIST_TRAVELED = "shape_dist_traveled"
}


/**
 * Reference: https://gtfs.org/documentation/schedule/reference/#stop_timestxt
 * Fields in stop_times.txt gtfs static data.
 */
@Serializable
data class GtfsStopTime(

    @SerialName(GtfsStopTimeField.TRIP_ID) val tripId: String,

    @SerialName(GtfsStopTimeField.STOP_ID) val stopId: String,

    @SerialName(GtfsStopTimeField.ARRIVAL_TIME) val arrivalTime: String,

    @SerialName(GtfsStopTimeField.DEPARTURE_TIME) val departureTime: String,

    @SerialName(GtfsStopTimeField.STOP_SEQUENCE) val stopSequence: Int,

    @SerialName(GtfsStopTimeField.STOP_HEADSIGN) val stopHeadsign: String? = null,

    @SerialName(GtfsStopTimeField.PICKUP_TYPE) val pickupType: Int? = null,

    @SerialName(GtfsStopTimeField.DROP_OFF_TYPE) val dropOffType: Int? = null,

    @SerialName(GtfsStopTimeField.TIMEPOINT) val timepoint: Int? = null,

    @SerialName(GtfsStopTimeField.STOP_NOTE) val stopNote: String? = null,

    @SerialName(GtfsStopTimeField.SHAPE_DIST_TRAVELED) val shapeDistTraveled: Double? = null,
)
