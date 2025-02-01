package app.krail.kgtfs.network

import app.krail.kgtfs.io.suspendSafeResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import java.nio.file.Paths

val projectRoot = Paths.get("").toAbsolutePath().toString()
val cacheDirectory = "$projectRoot/cache"

/**
 * Source: https://opendata.transport.nsw.gov.au/data/dataset/public-transport-timetables-realtime
 * API Swagger: https://opendata.transport.nsw.gov.au/data/dataset/public-transport-timetables-realtime/resource/9b3bfa13-0053-4008-8575-e30151f05d54
 */
object NswGtfsService {

    private val httpClient = getHttpClient()

    suspend fun fetchSydneyTrainsGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient
            .get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/sydneytrains")
            .validateHttpResponse("Sydney Trains")
    }

    suspend fun fetchSydneyMetroGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient
            .get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V2/metro")
            .validateHttpResponse("Sydney Metro")
    }

    suspend fun fetchBusesGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient
            .get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/buses")
            .validateHttpResponse("Buses")
    }

    suspend fun fetchLightRailGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/lightrail")
            .validateHttpResponse("Light Rail")
    }

    suspend fun fetchLightRailInnerwestGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/lightrail/innerwest")
            .validateHttpResponse("Light Rail innerwest")
    }

    suspend fun fetchLightRailNewcastleGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/lightrail/newcastle")
            .validateHttpResponse("Light Rail newcastle")
    }

    suspend fun fetchLightRailCbdAndSoutheastGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/lightrail/cbdandsoutheast")
            .validateHttpResponse("Light Rail cbdandsoutheast")
    }

    suspend fun fetchLightRailParramattaGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/lightrail/parramatta")
            .validateHttpResponse("Light Rail Parramatta")
    }

    suspend fun fetchNswTrainsGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/nswtrains")
            .validateHttpResponse("NSW Trains")
    }

    suspend fun fetchSydneyFerriesGtfs(): Result<HttpResponse> = suspendSafeResult(Dispatchers.Default) {
        httpClient.get("$NSW_TRANSPORT_BASE_URL/$GTFS_SCHEDULE_V1/ferries/sydneyferries")
            .validateHttpResponse("Sydney Ferries")
    }

    private fun HttpResponse.validateHttpResponse(apiName: String) =
        if (status == HttpStatusCode.OK) Result.success(this) else throw Exception("API Request for $apiName failed: $status")

    private const val NSW_TRANSPORT_BASE_URL = "https://api.transport.nsw.gov.au"
    private const val GTFS_SCHEDULE_V1 = "v1/gtfs/schedule"
    private const val GTFS_SCHEDULE_V2 = "v2/gtfs/schedule"
}
