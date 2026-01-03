package app.krail.kgtfs.io

import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.proto.NswStop
import app.krail.kgtfs.proto.NswStopList
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
            NswStop(
                stopId = stop.id,
                stopName = stop.name,
                lat = stop.lat.toDouble(),
                lon = stop.lon.toDouble(),
                productClass = stop.productClass.map { it },
                isParent = stop.isParent  // null (parent) or false (child)
            )
        }
        val protobufData = NswStopList(nswStops = stopList)
        val adapter = NswStopList.ADAPTER
        FileOutputStream(filePath).use { output ->
            output.write(adapter.encode(protobufData))
        }
        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )
        println("Encoded: ${stopList.size} - duration: $duration ms")
    }

    fun readProtoFile(filePath: String): NswStopList {
        val start = Clock.System.now()
        val decoded = FileInputStream(filePath).use { input ->
            NswStopList.ADAPTER.decode(input)
        }
        val duration = start.until(
            Clock.System.now(), DateTimeUnit.MILLISECOND, TimeZone.currentSystemDefault()
        )
        println("Decoded: ${decoded.nswStops.size} - duration: $duration ms")
        return decoded
    }
}
