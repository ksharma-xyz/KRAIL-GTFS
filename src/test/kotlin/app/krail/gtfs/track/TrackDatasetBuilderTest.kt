package app.krail.gtfs.track

import app.krail.kgtfs.track.Polyline
import app.krail.kgtfs.track.buildManifest
import app.krail.kgtfs.track.buildShapesDataset
import app.krail.kgtfs.track.buildTrackStops
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackDatasetBuilderTest {

    @Test
    fun `polyline encoding matches the canonical Google reference vector`() {
        val points = listOf(
            Polyline.Point(38.5, -120.2),
            Polyline.Point(40.7, -120.95),
            Polyline.Point(43.252, -126.453),
        )
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", Polyline.encode(points))
    }

    @Test
    fun `polyline round-trips Sydney coordinates within precision 5`() {
        val points = listOf(
            Polyline.Point(-33.8688, 151.2093),
            Polyline.Point(-33.8675, 151.2070),
            Polyline.Point(-33.9200, 151.0000),
        )
        val decoded = Polyline.decode(Polyline.encode(points))
        assertEquals(points.size, decoded.size)
        points.zip(decoded).forEach { (a, b) ->
            assertTrue(abs(a.lat - b.lat) < 1e-5 && abs(a.lon - b.lon) < 1e-5, "$a vs $b")
        }
    }

    @Test
    fun `directory keeps platforms AND stations with raw ids and parent links`() {
        val stops = buildTrackStops(
            listOf(
                mapOf(
                    "stop_id" to "200060", "stop_name" to "Central Station",
                    "stop_lat" to "-33.8832", "stop_lon" to "151.2065",
                    "location_type" to "1", "parent_station" to "",
                ),
                mapOf(
                    "stop_id" to "2000336", "stop_name" to "Central Station Platform 16",
                    "stop_lat" to "-33.8842", "stop_lon" to "151.2062",
                    "location_type" to "0", "parent_station" to "200060",
                ),
                mapOf(
                    "stop_id" to "X1", "stop_name" to "Station Entrance",
                    "stop_lat" to "-33.88", "stop_lon" to "151.20",
                    "location_type" to "2", "parent_station" to "200060",
                ),
            ),
        )
        assertEquals(listOf("200060", "2000336"), stops.map { it.stop_id }, "entrances excluded")
        val platform = stops.single { it.stop_id == "2000336" }
        assertEquals("Central Station Platform 16", platform.name)
        assertEquals("200060", platform.parent_id)
    }

    @Test
    fun `shapes dataset dedups trips onto shared shapes and prunes orphans`() {
        val ds = buildShapesDataset(
            bundleKey = "sydneytrains",
            version = "20260613",
            tripRows = listOf(
                mapOf("trip_id" to "trip-a", "shape_id" to "shape-1"),
                mapOf("trip_id" to "trip-b", "shape_id" to "shape-1"),
                mapOf("trip_id" to "trip-c", "shape_id" to "shape-orphan"),
            ),
            shapeRows = listOf(
                mapOf("shape_id" to "shape-1", "shape_pt_lat" to "-33.8688", "shape_pt_lon" to "151.2093", "shape_pt_sequence" to "2"),
                mapOf("shape_id" to "shape-1", "shape_pt_lat" to "-33.8675", "shape_pt_lon" to "151.2070", "shape_pt_sequence" to "1"),
                mapOf("shape_id" to "shape-unused", "shape_pt_lat" to "-30.0", "shape_pt_lon" to "150.0", "shape_pt_sequence" to "1"),
            ),
        )
        assertEquals(setOf("trip-a", "trip-b"), ds.trip_index.keys, "trip with missing shape dropped")
        assertEquals(setOf("shape-1"), ds.shapes.keys, "unused shape dropped")
        val decoded = Polyline.decode(ds.shapes["shape-1"]!!)
        assertTrue(abs(decoded.first().lat - -33.8675) < 1e-5, "points ordered by shape_pt_sequence")
        assertEquals("sydneytrains", ds.feed)
    }

    @Test
    fun `manifest carries url, sha256 and size per artifact`() {
        val manifest = buildManifest(
            version = "20260613",
            generatedAt = "2026-06-13T00:00:00Z",
            releaseUrlBase = "https://github.com/x/y/releases/download/track-latest",
            artifacts = listOf("track_stops.pb" to byteArrayOf(1, 2, 3)),
        )
        assertTrue("\"version\":\"20260613\"" in manifest)
        assertTrue("\"url\":\"https://github.com/x/y/releases/download/track-latest/track_stops.pb\"" in manifest)
        assertTrue("\"size_bytes\":3" in manifest)
        assertTrue("\"sha256\":\"" in manifest)
    }
}
