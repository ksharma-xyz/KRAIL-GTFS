package app.krail.kgtfs.filter

import app.krail.kgtfs.model.StopJson

/**
 * Folds ferry wharf stops that the TfNSW trip planner cannot route from onto the stop that
 * it can.
 *
 * GTFS publishes a stop per berth at the big wharf complexes, but the EFA journey planner
 * behind `/v1/tp/trip` does not know those ids at all. Asking it to plan from one does not
 * return an empty journey list, it returns `error BROKER -8020 origin: no matches`, and an
 * app that offers the stop shows a rider "no route found" for a ferry that is running.
 *
 * Each entry here is a berth id that is dead upstream, pointing at the complex id that is
 * live. Verified against `stop_finder?type_sf=stopID`, `departure_mon` and `trip`: the
 * berth ids resolve to nothing on all three, the targets resolve on all three.
 *
 * Adding a wharf is one line. Getting it wrong is not silent: [SydneyFerryFilterTest]
 * covers the merge, and every id here is one that upstream rejects today.
 */
object SydneyFerryFilter {

    /**
     * Where a dead wharf id goes, and what the surviving stop is called afterwards.
     *
     * The name matters because the survivor stands for the whole complex once its berths
     * are folded in. "Barangaroo Wharf 1" is the wrong label for a stop that now also means
     * wharf 2.
     */
    data class FerryStopReplacement(val targetId: String, val targetName: String)

    internal val REPLACEMENT_MAP: Map<String, FerryStopReplacement> = mapOf(
        // Manly. The three berth ids predate the current feed and are no longer published,
        // so these entries are dormant; they stay because the feed has reintroduced ids
        // before.
        "20951" to FerryStopReplacement("209573", "Manly Wharf"),
        "209525" to FerryStopReplacement("209573", "Manly Wharf"),
        "209593" to FerryStopReplacement("209573", "Manly Wharf"),

        // Circular Quay, wharves 2 to 6. Sydney's busiest ferry interchange, and every
        // berth id is unroutable. 200020 is the complex, and it is also the train and
        // light rail stop, which is why it keeps a mode-neutral name.
        "20003" to FerryStopReplacement("200020", "Circular Quay"),
        "20004" to FerryStopReplacement("200020", "Circular Quay"),
        "20005" to FerryStopReplacement("200020", "Circular Quay"),
        "20006" to FerryStopReplacement("200020", "Circular Quay"),
        "2000274" to FerryStopReplacement("200020", "Circular Quay"),

        // Barangaroo. Wharf 1's id is routable and wharf 2's is not, so the pair collapses
        // onto wharf 1.
        "2000442" to FerryStopReplacement("2000441", "Barangaroo Wharf"),
    )

    /**
     * Returns [data] with every id in [REPLACEMENT_MAP] folded into its target.
     *
     * The fold **merges product classes** rather than discarding the duplicate. That is the
     * whole reason this is not a `distinctBy`: Circular Quay arrives from the feed as a
     * train and light rail stop, and the only record carrying ferry is a berth that is about
     * to be redirected onto it. Drop the duplicate and the interchange ends up describing
     * itself as a place with no ferries, which is wrong in the data long before it is wrong
     * on a screen.
     *
     * A target that is already in [data] keeps its own coordinates and parent flag, because
     * the complex is a better anchor than any one berth. A target that is not in [data] is
     * created from the first berth that points at it.
     */
    fun processSydneyFerryData(data: List<StopJson>): List<StopJson> {
        val (toRedirect, toKeep) = data.partition { it.id in REPLACEMENT_MAP }

        if (toRedirect.isEmpty()) {
            println("SydneyFerryFilter: No stops to replace, returning original data.")
            return data
        }

        // Input order is preserved for everything that was already routable; a target that
        // only exists because of a redirect is appended.
        val byId = LinkedHashMap<String, StopJson>(data.size)
        toKeep.forEach { stop ->
            byId[stop.id] = stop.copy(productClass = stop.productClass.toMutableSet())
        }

        toRedirect.forEach { berth ->
            val replacement = REPLACEMENT_MAP.getValue(berth.id)
            val target = byId[replacement.targetId]

            if (target == null) {
                println(
                    "SydneyFerryFilter: Replacing stop ID ${berth.id} with " +
                        "${replacement.targetId} (target absent, creating it)",
                )
                byId[replacement.targetId] = berth.copy(
                    id = replacement.targetId,
                    name = replacement.targetName,
                    productClass = berth.productClass.toMutableSet(),
                    // The survivor stands for the whole complex, never for one berth.
                    isParent = null,
                )
            } else {
                println(
                    "SydneyFerryFilter: Replacing stop ID ${berth.id} with " +
                        "${replacement.targetId} (merging into existing target)",
                )
                target.productClass.addAll(berth.productClass)
                target.name = replacement.targetName
            }
        }

        return byId.values.toList()
    }
}
