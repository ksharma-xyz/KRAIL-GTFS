package app.krail.kgtfs.track

import app.krail.kgtfs.io.FileStorage.saveFile
import app.krail.kgtfs.io.ZipFileManager.unzip
import app.krail.kgtfs.network.NswGtfsService
import app.krail.kgtfs.network.cacheDirectory
import app.krail.kgtfs.nsw.NswTransportModeType
import app.krail.kgtfs.proto.ShapesDataset
import app.krail.kgtfs.proto.TrackStop
import app.krail.kgtfs.proto.TrackStopsDataset
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Builds the tracking datasets consumed by KRAIL-BFF live trip tracking
 * (see TrackDataset.proto for the wire contract):
 *
 *  - `track_stops.pb`     — platform-level stop directory, merged across modes
 *  - `shapes_<bundle>.pb` — per-mode shape_id → encoded polyline + trip index
 *  - `track_manifest.json`— version + per-artifact url/sha256/size
 *
 * Standalone entry point — does NOT touch the existing stops/routes
 * pipeline (Main.kt). Downloads its own GTFS bundles into the same cache
 * directory layout, then derives the track artifacts.
 *
 * Buses are intentionally excluded: bus shapes are 10–50× larger and the
 * BFF falls back to straight-line geometry for buses until that's solved.
 *
 * Usage:
 *   ./gradlew buildTrackDataset \
 *     [-PoutDir=cache/track] [-Pversion=YYYYMMDD] [-PreleaseUrlBase=https://...]
 */
fun main(args: Array<String>) = runBlocking {
    val outDir = args.getOrNull(0)?.takeIf { it.isNotBlank() } ?: "$cacheDirectory/track"
    val version = args.getOrNull(1)?.takeIf { it.isNotBlank() }
        ?: LocalDate.now(ZoneId.of("Australia/Sydney")).format(DateTimeFormatter.BASIC_ISO_DATE)
    val releaseUrlBase = (args.getOrNull(2)?.takeIf { it.isNotBlank() }
        ?: "https://github.com/ksharma-xyz/KRAIL-GTFS/releases/download/track-latest").trimEnd('/')

    println("Building track datasets v$version -> $outDir")

    val out = File(outDir).apply { mkdirs() }
    val artifacts = mutableListOf<Pair<String, ByteArray>>() // name -> bytes
    val mergedStops = LinkedHashMap<String, TrackStop>()

    for (trackMode in trackModes) {
        val modeName = trackMode.mode.modeName
        val gtfsDir = File("$cacheDirectory/$modeName")
        try {
            println("$modeName: downloading GTFS bundle")
            val response = trackMode.fetch().getOrThrow()
            saveFile(fileName = "$modeName.zip", data = response.readRawBytes(), directory = cacheDirectory)
            unzip(
                zipPath = "$cacheDirectory/$modeName.zip".toPath(),
                destinationPath = "$cacheDirectory/$modeName".toPath(),
            )
        } catch (e: Exception) {
            println("WARN: $modeName fetch failed (${e.message}); skipping mode")
            continue
        }

        val stopsFile = File(gtfsDir, "stops.txt")
        if (stopsFile.exists()) {
            for (stop in buildTrackStops(GtfsCsv.readWithHeader(stopsFile))) {
                mergedStops.putIfAbsent(stop.stop_id, stop)
            }
        }

        val tripsFile = File(gtfsDir, "trips.txt")
        val shapesFile = File(gtfsDir, "shapes.txt")
        if (!tripsFile.exists() || !shapesFile.exists()) {
            println("$modeName: no trips.txt/shapes.txt — skipping shapes")
            continue
        }
        val dataset = buildShapesDataset(
            bundleKey = trackMode.bundleKey,
            version = version,
            tripRows = GtfsCsv.readWithHeader(tripsFile),
            shapeRows = GtfsCsv.readWithHeader(shapesFile),
        )
        if (dataset.shapes.isEmpty()) {
            println("$modeName: 0 usable shapes — skipping")
            continue
        }
        artifacts.add("shapes_${trackMode.bundleKey}.pb" to dataset.encode())
        println("  shapes_${trackMode.bundleKey}.pb: ${dataset.shapes.size} shapes, ${dataset.trip_index.size} trips")
    }

    val directory = TrackStopsDataset(
        version = version,
        generated_at = Instant.now().toString(),
        attribution = "Data © Transport for NSW (CC BY 4.0). Modified by KRAIL: GTFS → protobuf.",
        stops = mergedStops.values.toList(),
    )
    artifacts.add(0, "track_stops.pb" to directory.encode())
    println("  track_stops.pb: ${mergedStops.size} stops")

    check(mergedStops.isNotEmpty()) { "no stops parsed from any bundle — refusing to publish an empty directory" }

    for ((name, bytes) in artifacts) File(out, name).writeBytes(bytes)
    File(out, "track_manifest.json").writeText(buildManifest(version, directory.generated_at, releaseUrlBase, artifacts))
    println("Wrote ${artifacts.size} artifacts + track_manifest.json -> ${out.absolutePath}")
}

private class TrackMode(
    val mode: NswTransportModeType,
    val bundleKey: String,
    val fetch: suspend () -> Result<HttpResponse>,
)

/**
 * bundleKey must match KRAIL-BFF TrackDatasetStore.bundleKeyFor(): the
 * per-line lightrail realtime feeds all map to "lightrail"; ferries map
 * to "ferries_sydneyferries".
 */
private val trackModes = listOf(
    TrackMode(NswTransportModeType.SYDNEY_TRAINS, "sydneytrains") { NswGtfsService.fetchSydneyTrainsGtfs() },
    TrackMode(NswTransportModeType.NSW_TRAINS, "nswtrains") { NswGtfsService.fetchNswTrainsGtfs() },
    TrackMode(NswTransportModeType.SYDNEY_METRO, "metro") { NswGtfsService.fetchSydneyMetroGtfs() },
    TrackMode(NswTransportModeType.LIGHT_RAIL, "lightrail") { NswGtfsService.fetchLightRailGtfs() },
    TrackMode(NswTransportModeType.SYDNEY_FERRY, "ferries_sydneyferries") { NswGtfsService.fetchSydneyFerriesGtfs() },
)

/** Platforms/stops (location_type 0) and stations (1) only — GTFS-R never references other types. */
internal fun buildTrackStops(rows: List<Map<String, String>>): List<TrackStop> =
    rows.mapNotNull { row ->
        val id = row["stop_id"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val locationType = row["location_type"]?.toIntOrNull() ?: 0
        if (locationType != 0 && locationType != 1) return@mapNotNull null
        val name = row["stop_name"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        TrackStop(
            stop_id = id,
            name = name,
            parent_id = row["parent_station"].orEmpty(),
            lat = row["stop_lat"]?.toDoubleOrNull() ?: 0.0,
            lon = row["stop_lon"]?.toDoubleOrNull() ?: 0.0,
        )
    }

internal fun buildShapesDataset(
    bundleKey: String,
    version: String,
    tripRows: List<Map<String, String>>,
    shapeRows: List<Map<String, String>>,
): ShapesDataset {
    // trips.txt: trip_id → shape_id (many → one)
    val tripIndex = tripRows.mapNotNull { row ->
        val tripId = row["trip_id"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val shapeId = row["shape_id"]?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        tripId to shapeId
    }.toMap()

    // shapes.txt: shape_id → ordered points → encoded polyline
    data class Pt(val seq: Int, val lat: Double, val lon: Double)
    val pointsByShape = HashMap<String, MutableList<Pt>>()
    for (row in shapeRows) {
        val id = row["shape_id"]?.takeIf { it.isNotBlank() } ?: continue
        val lat = row["shape_pt_lat"]?.toDoubleOrNull() ?: continue
        val lon = row["shape_pt_lon"]?.toDoubleOrNull() ?: continue
        val seq = row["shape_pt_sequence"]?.toIntOrNull() ?: continue
        pointsByShape.getOrPut(id) { mutableListOf() }.add(Pt(seq, lat, lon))
    }
    val polylines = pointsByShape.mapValues { (_, pts) ->
        Polyline.encode(pts.sortedBy { it.seq }.map { Polyline.Point(it.lat, it.lon) })
    }

    // Drop trips pointing at missing shapes; drop shapes no trip uses.
    val usedTripIndex = tripIndex.filterValues { it in polylines }
    val usedShapes = polylines.filterKeys { it in usedTripIndex.values.toSet() }

    return ShapesDataset(
        version = version,
        feed = bundleKey,
        shapes = usedShapes,
        trip_index = usedTripIndex,
    )
}

internal fun buildManifest(
    version: String,
    generatedAt: String,
    releaseUrlBase: String,
    artifacts: List<Pair<String, ByteArray>>,
): String = buildString {
    append("{\"version\":\"").append(version).append("\",")
    append("\"generated_at\":\"").append(generatedAt).append("\",")
    append("\"artifacts\":[")
    artifacts.forEachIndexed { i, (name, bytes) ->
        if (i > 0) append(',')
        append("{\"name\":\"").append(name).append("\",")
        append("\"url\":\"").append(releaseUrlBase).append('/').append(name).append("\",")
        append("\"sha256\":\"").append(sha256Hex(bytes)).append("\",")
        append("\"size_bytes\":").append(bytes.size).append('}')
    }
    append("]}")
}

private fun sha256Hex(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
