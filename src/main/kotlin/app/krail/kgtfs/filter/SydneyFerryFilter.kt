package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.NswTransportModeType

object SydneyFerryFilter {

    private val REPLACEMENT_MAP = mapOf(
        "20951" to Pair("209573", "Manly Wharf"),
        "209525" to Pair("209573", "Manly Wharf"),
        "209593" to Pair("209573", "Manly Wharf")
        // Add more replacement rules here in the future.
    )

    fun processSydneyFerryData(data: List<StopJson>): List<StopJson> {
        val processedList = data.map { stop ->
            REPLACEMENT_MAP[stop.id]?.let { (newId, _) ->
                stop.copy(id = newId)
            } ?: stop
        }

        // Remove duplicates that may have been introduced by the mapping.
        return processedList.distinctBy { it.id }
    }
}