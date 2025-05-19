package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson

object BusStopsFilter {

    fun filterOutBusStandData(data: List<StopJson>): List<StopJson> {
        val pattern = Regex("([,-]?\\s*Stand\\s+[A-Za-z0-9/]+)\\b")

        // Pair each stop with its cleaned name
        val cleanedPairs = data.map { stop ->
            stop.copy(name = stop.name.replace(pattern, "").trim()) to stop
        }

        // Group by cleaned name
        return cleanedPairs.groupBy { it.first.name }
            .map { (cleanedName, pairs) ->
                // Find the original entry without stand info
                val originalStop = pairs.find { !pattern.containsMatchIn(it.second.name) }?.second ?: pairs.first().second
                val mergedProductClass = pairs.map { it.first.productClass }.flatten().toSet()
                originalStop.copy(name = cleanedName, productClass = mergedProductClass.toMutableSet())
            }
    }
}