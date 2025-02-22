package app.krail.kgtfs.io

import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.proto.KrailNswStop
import app.krail.kgtfs.proto.KrailNswStopList
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.until
import java.io.FileOutputStream
import java.io.FileInputStream

object NswStopsProtoIO {

    fun writeProtoFile(data: List<StopJson>, filePath: String) {
        val start = Clock.System.now()
        val stopList = data.map { stop ->
            KrailNswStop(
                stopId = stop.id,
                stopName = stop.name,
                lat = stop.lat.toDouble(),
                lon = stop.lon.toDouble(),
                productClass = stop.productClass.map { it }
            )
        }
        val protobufData = KrailNswStopList(nswStops = stopList)
        val adapter = KrailNswStopList.ADAPTER
        FileOutputStream(filePath).use { output ->
            output.write(adapter.encode(protobufData))
        }
        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )
        println("Encoded: ${stopList.size} - duration: $duration ms")
    }

    fun readProtoFile(filePath: String): KrailNswStopList {
        val start = Clock.System.now()
        val decoded = FileInputStream(filePath).use { input ->
            KrailNswStopList.ADAPTER.decode(input)
        }
        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )
        println("Decoded: ${decoded.nswStops.size} - duration: $duration ms")
        return decoded
    }
}
