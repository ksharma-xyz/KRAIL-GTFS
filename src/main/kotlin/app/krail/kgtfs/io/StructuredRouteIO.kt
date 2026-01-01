package app.krail.kgtfs.io

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.*
import app.krail.kgtfs.repository.GtfsRepository
import app.krail.kgtfs.proto.NswBusRouteList
import app.krail.kgtfs.proto.NswBusRouteGroup
import app.krail.kgtfs.proto.NswBusRouteVariant
import app.krail.kgtfs.proto.NswBusTripOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.io.FileOutputStream

object StructuredRouteIO {

    suspend fun writeStructuredRoutes(
        repository: GtfsRepository,
        transportMode: String,
        filePrefix: String,
        exportProtobuf: Boolean = true
    ) = withContext(Dispatchers.IO) {

        val structuredData = repository.getStructuredRouteData().getOrNull()

        if (structuredData != null) {
            val exportData = StructuredRouteData(
                transportMode = transportMode,
                generatedAt = Instant.now().toString(),
                routes = structuredData
            )

            // Write JSON (Pretty)
            writeJsonToFile(
                data = exportData,
                path = AppConstants.CACHE_DIR_PATH,
                fileName = filePrefix,
                pretty = true
            )

            // Write JSON (Compact)
             writeJsonToFile(
                data = exportData,
                path = AppConstants.CACHE_DIR_PATH,
                fileName = filePrefix,
                pretty = false
            )

            // Write Protobuf
            if (exportProtobuf) {
                try {
                    val pbFileName = if (filePrefix == AppConstants.OutputFiles.NSW_BUSES_ROUTES) {
                        AppConstants.OutputFiles.NSW_BUSES_ROUTES_PB
                    } else {
                        "$filePrefix${AppConstants.FileExtensions.PROTOBUF}"
                    }
                    writeProtoFile(exportData, "${AppConstants.CACHE_DIR_PATH}/$pbFileName")
                } catch (e: Exception) {
                    println("[$transportMode] ⚠ Failed to export Protobuf: ${e.message}")
                }
            }

            println("[$transportMode] ✓ Exported structured route data")
        } else {
            println("[$transportMode] ⚠ Failed to generate structured route data")
        }
    }

    private fun writeProtoFile(data: StructuredRouteData, filePath: String) {
        val routeGroups = data.routes.map { (shortName, variants) ->
            val protoVariants = variants.map { variant ->
                val protoTrips = variant.trips.map { trip ->
                    NswBusTripOption(
                        tripId = trip.tripId,
                        headsign = trip.headsign,
                        stopIds = trip.stopIds
                    )
                }

                NswBusRouteVariant(
                    routeId = variant.routeId,
                    routeName = variant.routeName ?: "",
                    trips = protoTrips
                )
            }

            NswBusRouteGroup(
                routeShortName = shortName,
                variants = protoVariants
            )
        }

        val protobufData = NswBusRouteList(
            transportMode = data.transportMode,
            generatedAt = data.generatedAt,
            routes = routeGroups
        )

        val adapter = NswBusRouteList.ADAPTER
        FileOutputStream(filePath).use { output ->
            output.write(adapter.encode(protobufData))
        }
        println("✓ Exported Protobuf: $filePath")
    }
}

