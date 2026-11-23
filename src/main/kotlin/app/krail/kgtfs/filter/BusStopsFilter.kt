package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.NswTransportModeType

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
            val representative = pickRepresentative(stops, pattern)

            // Create parent record
            val parent = createParent(cleanedName, representative, stops)

            if (!preserveChildren || stops.size == 1) {
                // Legacy mode or single stop (no children) - return parent only
                listOf(parent)
            } else {
                // New mode - return parent + all children
                val children = createChildren(stops, representative, pattern)
                listOf(parent) + children
            }
        }
    }

    /**
     * Picks the stop that stands for the whole group: its id, lat and lon become the parent's.
     *
     * Order of preference:
     * 1. A stop without stand info that serves a mode other than bus. A bus stop can be named
     *    exactly like the station it serves (West Ryde Station is both rail station 211420 and
     *    bus stop 211425), and buses are merged first, so "first without stand info" used to
     *    hand the parent to the bus stop and silently drop the rail station: it vanished from
     *    the stops table, from search and from the Park & Ride mapping.
     * 2. Any stop without stand info.
     * 3. The first stop.
     */
    private fun pickRepresentative(stops: List<StopJson>, pattern: Regex): StopJson {
        val withoutStand = stops.filter { !pattern.containsMatchIn(it.name) }
        return withoutStand.firstOrNull { stop -> stop.productClass.any { it != BUS_PRODUCT_CLASS } }
            ?: withoutStand.firstOrNull()
            ?: stops.first()
    }

    /**
     * Creates a parent record with cleaned name and merged productClass.
     */
    private fun createParent(
        cleanedName: String,
        representative: StopJson,
        stops: List<StopJson>,
    ): StopJson {
        val mergedProductClass = stops.flatMap { it.productClass }.toMutableSet()

        return representative.copy(
            name = cleanedName,
            productClass = mergedProductClass,
            isParent = null  // null = default true (parent)
        )
    }

    /**
     * Creates child records preserving original names and individual attributes.
     *
     * Children are the actual stands/platforms, plus any stop that lost the parent slot to a
     * stop of a different mode (a bus stop named like its rail station). The second kind keeps
     * its own id, so a trip saved against that id still resolves. Same-mode stops that share a
     * name without stand info (bus-vs-bus, light rail platform pairs) are still collapsed into
     * the parent, as before.
     */
    private fun createChildren(
        stops: List<StopJson>,
        representative: StopJson,
        pattern: Regex
    ): List<StopJson> {
        return stops
            .filter { stop ->
                pattern.containsMatchIn(stop.name) ||
                    (stop !== representative && stop.productClass != representative.productClass)
            }
            .map { child ->
                child.copy(isParent = false)  // Mark as child
            }
    }

    private val BUS_PRODUCT_CLASS = NswTransportModeType.BUSES.productClass
}