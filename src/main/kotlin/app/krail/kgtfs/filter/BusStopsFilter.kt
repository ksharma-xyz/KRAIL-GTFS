package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson

object BusStopsFilter {

    /**
     * Filters and optionally preserves bus stand/platform data.
     *
     * @param data Raw stop data from GTFS
     * @param preserveChildren If true, returns parent + all children with isParent flag.
     *                         If false, collapses to parent only (legacy behavior).
     * @return Flat list of StopJson (parents + optional children)
     */
    fun filterOutBusStandData(
        data: List<StopJson>,
        preserveChildren: Boolean = false
    ): List<StopJson> {
        val pattern = Regex("([,-]?\\s*Stand\\s+[A-Za-z0-9/]+)\\b")

        // Group stops by cleaned name
        val groupedByCleanedName = data.groupBy { stop ->
            stop.name.replace(pattern, "").trim()
        }

        return groupedByCleanedName.flatMap { (cleanedName, stops) ->
            // Create parent record
            val parent = createParent(cleanedName, stops, pattern)

            if (!preserveChildren || stops.size == 1) {
                // Legacy mode or single stop (no children) - return parent only
                listOf(parent)
            } else {
                // New mode - return parent + all children
                val children = createChildren(stops, pattern)
                listOf(parent) + children
            }
        }
    }

    /**
     * Creates a parent record with cleaned name and merged productClass.
     */
    private fun createParent(
        cleanedName: String,
        stops: List<StopJson>,
        pattern: Regex
    ): StopJson {
        // Prefer stop without stand info for parent lat/lon, or use first
        val representative = stops.find { !pattern.containsMatchIn(it.name) } ?: stops.first()
        val mergedProductClass = stops.flatMap { it.productClass }.toMutableSet()

        return representative.copy(
            name = cleanedName,
            productClass = mergedProductClass,
            isParent = null  // null = default true (parent)
        )
    }

    /**
     * Creates child records preserving original names and individual attributes.
     */
    private fun createChildren(
        stops: List<StopJson>,
        pattern: Regex
    ): List<StopJson> {
        return stops
            .filter { pattern.containsMatchIn(it.name) }  // Only actual stands/platforms
            .map { child ->
                child.copy(isParent = false)  // Mark as child
            }
    }
}