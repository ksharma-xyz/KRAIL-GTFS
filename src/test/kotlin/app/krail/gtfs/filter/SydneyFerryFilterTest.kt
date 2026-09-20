package app.krail.gtfs.filter

import app.krail.kgtfs.filter.SydneyFerryFilter
import app.krail.kgtfs.filter.SydneyFerryFilter.processSydneyFerryData
import app.krail.kgtfs.model.StopJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The berth ids in [SydneyFerryFilter.REPLACEMENT_MAP] do not exist upstream. Asking the
 * TfNSW planner to route from one answers `error BROKER -8020 origin: no matches`, so a stop
 * that survives this filter with a berth id is a stop the app can offer and then fail to
 * plan from.
 *
 * These tests hold two things: that no berth id survives, and that folding a berth into its
 * complex does not lose the ferry product class on the way.
 */
class SydneyFerryFilterTest {

    private fun stop(
        id: String,
        name: String,
        productClass: Set<Int>,
        lat: String = "-33.86",
        lon: String = "151.21",
        isParent: Boolean? = null,
    ) = StopJson(
        id = id,
        name = name,
        lat = lat,
        lon = lon,
        productClass = productClass.toMutableSet(),
        isParent = isParent,
    )

    @Test
    fun `Circular Quay berths fold into the one routable stop`() {
        val input = listOf(
            stop("200020", "Circular Quay Station", setOf(TRAIN, LIGHT_RAIL)),
            stop("20003", "Circular Quay Wharf 2", setOf(FERRY)),
            stop("20004", "Circular Quay Wharf 3", setOf(FERRY)),
            stop("20005", "Circular Quay Wharf 4", setOf(FERRY)),
            stop("20006", "Circular Quay Wharf 5", setOf(FERRY)),
            stop("2000274", "Circular Quay Wharf 6", setOf(FERRY)),
        )

        val result = processSydneyFerryData(input)

        assertEquals(1, result.size, "five berths and the complex collapse to one stop")
        assertEquals("200020", result.single().id)
        assertEquals("Circular Quay", result.single().name)
    }

    @Test
    fun `folding a berth in keeps the ferry product class`() {
        // The regression this filter exists to prevent. A plain distinctBy would keep the
        // first record, which is the train and light rail one, and Sydney's busiest ferry
        // interchange would describe itself as having no ferries.
        val input = listOf(
            stop("200020", "Circular Quay Station", setOf(TRAIN, LIGHT_RAIL)),
            stop("20006", "Circular Quay Wharf 5", setOf(FERRY)),
        )

        val result = processSydneyFerryData(input)

        assertEquals(
            setOf(TRAIN, LIGHT_RAIL, FERRY),
            result.single().productClass,
            "the ferry class arrives only on the berth, so it has to survive the merge",
        )
    }

    @Test
    fun `the surviving stop keeps the complex coordinates, not the berth ones`() {
        val input = listOf(
            stop("200020", "Circular Quay Station", setOf(TRAIN), lat = "-33.8610", lon = "151.2106"),
            stop("20006", "Circular Quay Wharf 5", setOf(FERRY), lat = "-33.8599", lon = "151.2119"),
        )

        val result = processSydneyFerryData(input).single()

        assertEquals("-33.8610", result.lat)
        assertEquals("151.2106", result.lon)
    }

    @Test
    fun `Barangaroo wharf 2 folds into wharf 1 and the survivor is renamed`() {
        val input = listOf(
            stop("2000441", "Barangaroo Wharf 1", setOf(FERRY)),
            stop("2000442", "Barangaroo Wharf 2", setOf(FERRY)),
        )

        val result = processSydneyFerryData(input)

        assertEquals(1, result.size)
        assertEquals("2000441", result.single().id)
        assertEquals(
            "Barangaroo Wharf",
            result.single().name,
            "wharf 1 is the wrong label once it also stands for wharf 2",
        )
    }

    @Test
    fun `a target missing from the feed is created from the berth`() {
        val input = listOf(stop("20006", "Circular Quay Wharf 5", setOf(FERRY), isParent = false))

        val result = processSydneyFerryData(input).single()

        assertEquals("200020", result.id)
        assertEquals("Circular Quay", result.name)
        assertEquals(setOf(FERRY), result.productClass)
        assertNull(result.isParent, "the survivor stands for the complex, so it is a parent")
    }

    @Test
    fun `no berth id survives the filter`() {
        // Covers every key in the map at once, so adding a wharf without a target keeps
        // being caught here rather than in a rider's trip.
        val input = SydneyFerryFilter.REPLACEMENT_MAP.keys.mapIndexed { index, id ->
            stop(id, "Dead berth $index", setOf(FERRY))
        }

        val survivingIds = processSydneyFerryData(input).map { it.id }.toSet()

        assertTrue(
            survivingIds.none { it in SydneyFerryFilter.REPLACEMENT_MAP },
            "unroutable ids left in the output: " +
                survivingIds.filter { it in SydneyFerryFilter.REPLACEMENT_MAP },
        )
    }

    @Test
    fun `stops outside the map are untouched and keep their order`() {
        val input = listOf(
            stop("20601", "McMahons Point Wharf", setOf(FERRY)),
            stop("20006", "Circular Quay Wharf 5", setOf(FERRY)),
            stop("20611", "Milsons Point Wharf", setOf(FERRY)),
            stop("200060", "Central Station", setOf(TRAIN)),
        )

        val result = processSydneyFerryData(input)

        assertEquals(
            listOf("20601", "20611", "200060", "200020"),
            result.map { it.id },
            "untouched stops keep feed order; a target created by a redirect is appended",
        )
        assertEquals("McMahons Point Wharf", result.first().name)
    }

    @Test
    fun `a feed with nothing to replace is returned unchanged`() {
        val input = listOf(
            stop("20601", "McMahons Point Wharf", setOf(FERRY)),
            stop("200060", "Central Station", setOf(TRAIN)),
        )

        val result = processSydneyFerryData(input)

        assertSame(input, result, "no work to do, so no copying either")
    }

    @Test
    fun `the input list is not mutated`() {
        // processSydneyFerryData merges product classes, and doing that in place would
        // corrupt the caller's data for every later stage of the pipeline.
        val complex = stop("200020", "Circular Quay Station", setOf(TRAIN, LIGHT_RAIL))
        val input = listOf(complex, stop("20006", "Circular Quay Wharf 5", setOf(FERRY)))

        processSydneyFerryData(input)

        assertEquals(setOf(TRAIN, LIGHT_RAIL), complex.productClass)
        assertEquals("Circular Quay Station", complex.name)
    }

    private companion object {
        const val TRAIN = 1
        const val LIGHT_RAIL = 4
        const val FERRY = 9
    }
}
