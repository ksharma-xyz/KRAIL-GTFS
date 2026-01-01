package app.krail.kgtfs.io

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.model.MinimalRouteStopsJson
import app.krail.kgtfs.proto.NswRoute
import app.krail.kgtfs.proto.NswRouteList
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.until
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles Protocol Buffer serialization/deserialization for route data.
 * Similar to NswStopsProtoIO but for route-to-stops mappings.
 */
object RouteStopsProtoIO {

    /**
     * Write route data to Protocol Buffer file.
     *
     * @param data Minimal route data structure
     * @param filePath Output .pb file path
     */
    fun writeProtoFile(data: MinimalRouteStopsJson, filePath: String) {
        val start = Clock.System.now()

        val routeList = data.routes.map { (routeNumber, stopIds) ->
            NswRoute(
                routeNumber = routeNumber,
                stopIds = stopIds
            )
        }

        val protobufData = NswRouteList(
            transportMode = data.transportMode,
            totalRoutes = data.totalRoutes,
            generatedAt = data.generatedAt,
            routes = routeList
        )

        val adapter = NswRouteList.ADAPTER
        FileOutputStream(filePath).use { output ->
            output.write(adapter.encode(protobufData))
        }

        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )

        println("[RouteProto] Encoded ${routeList.size} routes in $duration ms")
    }

    /**
     * Read route data from Protocol Buffer file.
     *
     * @param filePath Input .pb file path
     * @return Decoded route list
     */
    fun readProtoFile(filePath: String): NswRouteList {
        val start = Clock.System.now()

        val decoded = FileInputStream(filePath).use { input ->
            NswRouteList.ADAPTER.decode(input)
        }

        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )

        println("[RouteProto] Decoded ${decoded.routes.size} routes in $duration ms")
        return decoded
    }

    /**
     * Convert decoded protobuf back to MinimalRouteStopsJson.
     */
    fun toMinimalRouteStopsJson(nswRouteList: NswRouteList): MinimalRouteStopsJson {
        return MinimalRouteStopsJson(
            transportMode = nswRouteList.transportMode,
            totalRoutes = nswRouteList.totalRoutes,
            generatedAt = nswRouteList.generatedAt,
            routes = nswRouteList.routes.associate { route ->
                route.routeNumber to route.stopIds
            }
        )
    }
}

