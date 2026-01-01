package app.krail.kgtfs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured route data for user selection flow.
 *
 * Flow:
 * 1. User searches by short name (e.g. "702") -> gets list of [RouteVariant]
 * 2. User selects variant (e.g. "Sydney Buses Network") -> gets list of [RouteDirection]
 * 3. User selects direction -> gets list of stop IDs
 */
@Serializable
data class StructuredRouteData(
    @SerialName("transport_mode")
    val transportMode: String,

    @SerialName("generated_at")
    val generatedAt: String,

    // Key: route_short_name (e.g. "702")
    @SerialName("routes")
    val routes: Map<String, List<RouteVariant>>
)

@Serializable
data class RouteVariant(
    @SerialName("route_id")
    val routeId: String, // Unique ID (e.g. "2504_702")

    @SerialName("route_name")
    val routeName: String?, // From route_long_name (e.g. "Blacktown to Seven Hills")

    @SerialName("trips")
    val trips: List<TripOption>
)

@Serializable
data class TripOption(
    @SerialName("trip_id")
    val tripId: String, // Representative trip ID

    @SerialName("headsign")
    val headsign: String, // The "route_direction" or headsign

    @SerialName("stop_ids")
    val stopIds: List<String>
)

