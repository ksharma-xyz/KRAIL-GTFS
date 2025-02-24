package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson

object BusStopsFilter {

    fun filterOutBusStandData(data: List<StopJson>): List<StopJson> {
        val pattern = Regex("([,-]?\\s*Stand\\s+[A-Za-z0-9/]+)\\b")

        // Normalize names by removing 'Stand' followed by letters, numbers, or fractions from every entry
        val cleanedData = data.map { stop ->
            stop.copy(name = stop.name.replace(pattern, "").trim())
        }

        // Merge entries with the same cleaned-up name
        return cleanedData.groupBy { it.name }
            .map { (key, stops) ->
                stops.reduce { acc, stop ->
                    val mergedProductClass = acc.productClass.toMutableSet().apply { addAll(stop.productClass) }
                    acc.copy(name = key, productClass = mergedProductClass)
                }
            }
    }
}
