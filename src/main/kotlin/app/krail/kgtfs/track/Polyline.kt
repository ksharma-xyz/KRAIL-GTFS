package app.krail.kgtfs.track

/**
 * Google polyline algorithm, precision 5 — the encoding the KRAIL-BFF
 * track contract promises in `LegGeometry.encoded_polyline`. Encode is
 * used by the track dataset builder; decode exists for tests.
 */
object Polyline {

    data class Point(val lat: Double, val lon: Double)

    fun encode(points: List<Point>): String {
        val sb = StringBuilder()
        var prevLat = 0L
        var prevLon = 0L
        for (p in points) {
            val lat = Math.round(p.lat * 1e5)
            val lon = Math.round(p.lon * 1e5)
            encodeValue(lat - prevLat, sb)
            encodeValue(lon - prevLon, sb)
            prevLat = lat
            prevLon = lon
        }
        return sb.toString()
    }

    fun decode(encoded: String): List<Point> {
        val points = mutableListOf<Point>()
        var index = 0
        var lat = 0L
        var lon = 0L
        while (index < encoded.length) {
            val (dLat, i1) = decodeValue(encoded, index)
            val (dLon, i2) = decodeValue(encoded, i1)
            index = i2
            lat += dLat
            lon += dLon
            points.add(Point(lat / 1e5, lon / 1e5))
        }
        return points
    }

    private fun encodeValue(value: Long, sb: StringBuilder) {
        var v = value shl 1
        if (value < 0) v = v.inv()
        while (v >= 0x20) {
            sb.append((((v and 0x1f) or 0x20) + 63).toInt().toChar())
            v = v shr 5
        }
        sb.append((v + 63).toInt().toChar())
    }

    private fun decodeValue(encoded: String, start: Int): Pair<Long, Int> {
        var index = start
        var result = 0L
        var shift = 0
        while (true) {
            val b = (encoded[index].code - 63).toLong()
            index++
            result = result or ((b and 0x1f) shl shift)
            shift += 5
            if (b < 0x20) break
        }
        val value = if (result and 1L != 0L) (result shr 1).inv() else result shr 1
        return value to index
    }
}
