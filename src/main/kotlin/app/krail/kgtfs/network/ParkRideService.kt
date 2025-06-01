package app.krail.kgtfs.network

import app.krail.kgtfs.io.suspendSafeResult
import app.krail.kgtfs.model.CarParkFacilityDetailResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

object ParkRideService {

    private val httpClient = getHttpClient()
    private const val NSW_TRANSPORT_BASE_URL = "https://api.transport.nsw.gov.au"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Makes a GET request to the given [url] with optional [params], validates the response,
     * and returns the response body as a string.
     *
     * @param url The endpoint URL.
     * @param description Description for error reporting.
     * @param params Optional query parameters.
     * @return The response body as text.
     * @throws Exception if the response is not successful.
     */
    private suspend fun getValidatedResponseBody(
        url: String,
        description: String,
        params: Map<String, String> = emptyMap()
    ): String {
        val response = httpClient
            .get(url) {
                params.forEach { (key, value) ->
                    url { parameters.append(key, value) }
                }
            }
            .validateHttpResponse(description)
            .getOrThrow()
        return response.bodyAsText()
    }

    /**
     * Returns the details of a car park facility by its ID.
     *
     * @param facilityId The facility ID to query.
     * @return [Result] containing [CarParkFacilityDetailResponse] on success, or an error on failure.
     */
    suspend fun getCarParkFacilitiesDetail(
        facilityId: String
    ): Result<CarParkFacilityDetailResponse> = suspendSafeResult(Dispatchers.Default) {
        val body = getValidatedResponseBody(
            url = "$NSW_TRANSPORT_BASE_URL/v1/carpark",
            description = "Car Park Facilities Detail for ID: $facilityId",
            params = mapOf("facility" to facilityId)
        )
        Result.success(json.decodeFromString<CarParkFacilityDetailResponse>(body))
    }

    /**
     * Returns a map of facility ID to facility name for all car parks.
     * Since facility ID is not specified, a list of facility names with their ID will be returned.
     *
     * @return [Result] containing a [Map] of facility IDs to names, or an error on failure.
     */
    suspend fun getCarParkFacilities(): Result<Map<String, String>> = suspendSafeResult(Dispatchers.Default) {
        val body = getValidatedResponseBody(
            url = "$NSW_TRANSPORT_BASE_URL/v1/carpark",
            description = "Car Park Facilities"
        )
        Result.success(json.decodeFromString<Map<String, String>>(body))
    }
}
