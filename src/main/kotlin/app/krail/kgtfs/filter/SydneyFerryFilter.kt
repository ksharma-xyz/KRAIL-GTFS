package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson

object SydneyFerryFilter {

    private val REPLACEMENT_MAP = mapOf(
        "20951" to Pair("209573", "Manly Wharf"),
        "209525" to Pair("209573", "Manly Wharf"),
        "209593" to Pair("209573", "Manly Wharf")
        // Add more replacements todo - for Circular Quay Wharf, and Barangaroo Wharf
    )

    fun processSydneyFerryData(data: List<StopJson>): List<StopJson> {
        // Partition the list into stops that need replacement and those that do not.
        val (toReplace, toKeep) = data.partition { it.id in REPLACEMENT_MAP }

        // If there's nothing to replace, return the original list to avoid further processing.
        if (toReplace.isEmpty()) {
            println("SydneyFerryFilter: No stops to replace, returning original data.")
            return data
        }

        // Map only the necessary stops to their new IDs.
        val replaced = toReplace.map { stop ->
            REPLACEMENT_MAP[stop.id]?.let { (newId, name) ->
                println("SydneyFerryFilter: Replacing stop ID ${stop.id} with $newId")
                stop.copy(id = newId, name = name)
            } ?: stop // Fallback, though partition ensures it's never null.
        }

        // Combine the lists and remove duplicates.
        return (toKeep + replaced).distinctBy { it.id }
    }
}