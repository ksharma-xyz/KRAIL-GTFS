package app.krail.kgtfs.nsw

import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.proto.NswStop

fun GtfsStop.toStopJson(transportModeType: NswTransportModeType): StopJson {
    return StopJson(
        id = stopId.toString(),
        name = name,
        lat = latitude?.toString() ?: "",
        lon = longitude?.toString() ?: "",
        productClass = mutableSetOf(transportModeType.productClass)
    )
}

fun StopJson.toKrailGtfsStop(): NswStop {
    return NswStop(
        stopId = id,
        stopName = name,
        lat = lat.toDouble(),
        lon = lon.toDouble(),
    )
}
