package app.krail.kgtfs.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * MINIMAL JSON format - only route number and ordered stop IDs.
 * Mobile app already has stop details (name, lat, lon) in local DB.
 * Array position = sequence (first element is sequence 1).
 */
@Serializable
data class MinimalRouteStopsJson(
    @SerialName("transport_mode") val transportMode: String,
    @SerialName("total_routes") val totalRoutes: Int,
    @SerialName("generated_at") val generatedAt: String,
    @SerialName("routes") val routes: Map<String, List<String>> // route_number -> [stop_ids]
)

