package app.krail.kgtfs.csv

import app.krail.kgtfs.model.*
import app.krail.kgtfs.nsw.NswTransportModeType
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path

object CsvReader {

    suspend inline fun <reified T> readCsvFile(
        path: Path,
        crossinline mapper: (Map<String, String>) -> T,
    ): List<T> = withContext(Dispatchers.IO) {
        val items = mutableListOf<T>()
        csvReader().openAsync(path.toString()) {
            readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->

                val item = mapper(row)
                if (item != null) {
                    items.add(item)
                }
            }
        }
        return@withContext items
    }

    suspend fun readGtfsStops(
        path: Path,
        nswTransportModeType: NswTransportModeType
    ): List<GtfsStop> = withContext(Dispatchers.IO) {
        val data = mutableListOf<GtfsStop>()
        try {
            data.addAll(readCsvFile(path = path) { dataMap ->
                val stopId = dataMap[GtfsStopField.STOP_ID]
                val stopName = dataMap[GtfsStopField.STOP_NAME]
                val stopLat = dataMap[GtfsStopField.STOP_LAT]?.toDoubleOrNull()
                val stopLon = dataMap[GtfsStopField.STOP_LON]?.toDoubleOrNull()
                val locationType = dataMap[GtfsStopField.LOCATION_TYPE]
                val parentStation = dataMap[GtfsStopField.PARENT_STATION]
                val finalStopId = parentStation?.takeIf { it.isNotEmpty() } ?: stopId

                if (finalStopId != null && stopName != null && stopLat != null && stopLon != null) {
                    when (nswTransportModeType) {
                        NswTransportModeType.SYDNEY_TRAINS,
                        NswTransportModeType.NSW_TRAINS,
                        NswTransportModeType.SYDNEY_METRO -> {
                            if (locationType == "1") {
                                GtfsStop(
                                    stopId = StopId(id = finalStopId),
                                    name = stopName,
                                    latitude = stopLat,
                                    longitude = stopLon,
                                )
                            } else {
                                null
                            }
                        }
                        else -> {
                            GtfsStop(
                                stopId = StopId(id = finalStopId),
                                name = stopName,
                                latitude = stopLat,
                                longitude = stopLon,
                            )
                        }
                    }
                } else {
                    null
                }
            }.filterNotNull())
        } catch (e: java.io.FileNotFoundException) {
            println("[StopReader] ERROR: File not found: $path")
            println("[StopReader] Run NswGtfsManager.fetch() to download GTFS data")
        } catch (e: Exception) {
            println("[StopReader] ERROR: Failed to parse stops - ${e.message}")
        }
        println("${nswTransportModeType.modeName}: Valid stops: ${data.size}")
        data
    }

    suspend fun readGtfsStopTimes(path: Path, nswTransportModeType: NswTransportModeType): List<GtfsStopTime> {
        val data = mutableListOf<GtfsStopTime>()
        var skippedRows = 0

        try {
            data.addAll(readCsvFile(path = path) { dataMap ->
                val stopId = dataMap[GtfsStopTimeField.STOP_ID]
                val tripId = dataMap[GtfsStopTimeField.TRIP_ID]
                val arrivalTime = dataMap[GtfsStopTimeField.ARRIVAL_TIME]
                val departureTime = dataMap[GtfsStopTimeField.DEPARTURE_TIME]
                val stopSequence = dataMap[GtfsStopTimeField.STOP_SEQUENCE]?.toIntOrNull()
                val stopHeadsign = dataMap[GtfsStopTimeField.STOP_HEADSIGN]
                val pickupType = dataMap[GtfsStopTimeField.PICKUP_TYPE]?.toIntOrNull()
                val dropOffType = dataMap[GtfsStopTimeField.DROP_OFF_TYPE]?.toIntOrNull()
                val timepoint = dataMap[GtfsStopTimeField.TIMEPOINT]?.toIntOrNull()
                val stopNote = dataMap[GtfsStopTimeField.STOP_NOTE]
                val shapeDistTraveled = dataMap[GtfsStopTimeField.SHAPE_DIST_TRAVELED]?.toDoubleOrNull()

                if (stopId != null && tripId != null && arrivalTime != null && departureTime != null && stopSequence != null) {
                    GtfsStopTime(
                        tripId = tripId,
                        stopId = stopId,
                        arrivalTime = arrivalTime,
                        departureTime = departureTime,
                        stopSequence = stopSequence,
                        stopHeadsign = stopHeadsign,
                        pickupType = pickupType,
                        dropOffType = dropOffType,
                        timepoint = timepoint,
                        stopNote = stopNote,
                        shapeDistTraveled = shapeDistTraveled,
                    )
                } else {
                    skippedRows++
                    null
                }
            }.filterNotNull())
        } catch (e: java.io.FileNotFoundException) {
            println("[StopTimeReader] ERROR: File not found: $path")
            return emptyList()
        } catch (e: Exception) {
            println("[StopTimeReader] ERROR: Failed to parse stop times - ${e.message}")
            return emptyList()
        }

        if (skippedRows > 0) {
            println("[StopTimeReader] WARNING: Skipped $skippedRows corrupt rows")
        }
        println("${nswTransportModeType.modeName}: Valid stop times: ${data.size}")
        return data
    }
}