package app.krail.kgtfs.model

import app.krail.kgtfs.AppConstants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Reference: https://gtfs.org/documentation/schedule/reference/#stop_timestxt
 * Fields in stop_times.txt gtfs static data.
 */
@Serializable
data class GtfsStopTime(

    @SerialName(AppConstants.GtfsFields.TRIP_ID) val tripId: String,

    @SerialName(AppConstants.GtfsFields.STOP_ID) val stopId: String,

    @SerialName(AppConstants.GtfsFields.ARRIVAL_TIME) val arrivalTime: String,

    @SerialName(AppConstants.GtfsFields.DEPARTURE_TIME) val departureTime: String,

    @SerialName(AppConstants.GtfsFields.STOP_SEQUENCE) val stopSequence: Int,

    @SerialName(AppConstants.GtfsFields.STOP_HEADSIGN) val stopHeadsign: String? = null,

    @SerialName(AppConstants.GtfsFields.PICKUP_TYPE) val pickupType: Int? = null,

    @SerialName(AppConstants.GtfsFields.DROP_OFF_TYPE) val dropOffType: Int? = null,

    @SerialName(AppConstants.GtfsFields.TIMEPOINT) val timepoint: Int? = null,

    @SerialName(AppConstants.GtfsFields.STOP_NOTE) val stopNote: String? = null,

    @SerialName(AppConstants.GtfsFields.SHAPE_DIST_TRAVELED) val shapeDistTraveled: Double? = null,
)
