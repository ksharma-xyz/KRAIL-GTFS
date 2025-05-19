package app.krail.gtfs.nsw

import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.StopId
import app.krail.kgtfs.nsw.NswGtfsManager.createCommonGtfsStops
import app.krail.kgtfs.nsw.NswTransportModeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NswGtfsManagerTest {

    private fun gtfsStop(id: String, name: String) = GtfsStop(
        stopId = StopId(id),
        name = name,
        latitude = 0.0,
        longitude = 0.0
    )

    /**
     * Verifies that stops with the same id from different modes are merged,
     * and their product classes are aggregated.
     */
    @Test
    fun `merges stops with same id and aggregates product classes`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("1", "Bus Stop 1")),
            NswTransportModeType.SYDNEY_FERRY to listOf(gtfsStop("1", "Ferry Stop 1"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertEquals(setOf(NswTransportModeType.BUSES.productClass, NswTransportModeType.SYDNEY_FERRY.productClass), result[0].productClass)
    }

    /**
     * Verifies that if any stop for an id contains "Coach Stop" in its name,
     * the COACH product class is added to the merged stop.
     */
    @Test
    fun `coach stop name or existing coach stop adds coach product class`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("2", "Coach Stop Central")),
            NswTransportModeType.SYDNEY_FERRY to listOf(gtfsStop("2", "Ferry Stop Central"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertTrue(result[0].productClass.contains(NswTransportModeType.COACH.productClass))
    }

    /**
     * Verifies that when merging stops with productClass 1,
     * the name from SYDNEY_TRAINS is always preferred.
     */
    @Test
    fun `always prefers SYDNEY_TRAINS name for productClass 1`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("3", "Bus Stop 3")),
            NswTransportModeType.SYDNEY_TRAINS to listOf(gtfsStop("3", "Sydney Trains Central"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertEquals("Sydney Trains Central", result[0].name)
    }

    /**
     * Verifies that if there are multiple SYDNEY_TRAINS entries for the same id,
     * the name from the last one is used.
     */
    @Test
    fun `multiple SYDNEY_TRAINS entries for same id uses last name`() {
        val map = mapOf(
            NswTransportModeType.SYDNEY_TRAINS to listOf(
                gtfsStop("4", "Sydney Trains Old"),
                gtfsStop("4", "Sydney Trains New")
            )
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertEquals("Sydney Trains New", result[0].name)
    }

    /**
     * Verifies that stops with different ids are not merged.
     */
    @Test
    fun `no merge for different ids`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("5", "Bus Stop 5")),
            NswTransportModeType.SYDNEY_FERRY to listOf(gtfsStop("6", "Ferry Stop 6"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == "5" })
        assertTrue(result.any { it.id == "6" })
    }

    /**
     * Verifies that if a stop's name contains "Coach Stop" and it is the only entry,
     * the product class is set to COACH.
     */
    @Test
    fun `coach stop only entry sets product class to coach`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("7", "Coach Stop Only"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertEquals(setOf(NswTransportModeType.COACH.productClass), result[0].productClass)
    }

    /**
     * Verifies that all product classes are merged for the same id,
     * and the name from the first encountered stop is kept.
     */
    @Test
    fun `merges all product classes for same id`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("8", "Bus Stop 8")),
            NswTransportModeType.SYDNEY_FERRY to listOf(gtfsStop("8", "Ferry Stop 8")),
            NswTransportModeType.LIGHT_RAIL to listOf(gtfsStop("8", "Light Rail Stop 8"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertTrue(result[0].productClass.containsAll(
            listOf(
                NswTransportModeType.BUSES.productClass,
                NswTransportModeType.SYDNEY_FERRY.productClass,
                NswTransportModeType.LIGHT_RAIL.productClass
            )
        ))
    }

    /**
     * Verifies that when merging stops with the same id and different names,
     * the name from the first encountered stop is kept (unless SYDNEY_TRAINS is present).
     */
    @Test
    fun `merges all product classes for same id and checks name merging`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("8", "Bus Stop 8")),
            NswTransportModeType.SYDNEY_FERRY to listOf(gtfsStop("8", "Ferry Stop 8")),
            NswTransportModeType.LIGHT_RAIL to listOf(gtfsStop("8", "Light Rail Stop 8"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertTrue(result[0].productClass.containsAll(
            listOf(
                NswTransportModeType.BUSES.productClass,
                NswTransportModeType.SYDNEY_FERRY.productClass,
                NswTransportModeType.LIGHT_RAIL.productClass
            )
        ))
        // Name should be from the first encountered stop ("Bus Stop 8")
        assertEquals("Bus Stop 8", result[0].name)
    }

    /**
     * Verifies that when both SYDNEY_TRAINS and other train/mode types are present,
     * the name from SYDNEY_TRAINS is always preferred.
     */
    @Test
    fun `merges name with SYDNEY_TRAINS preferred over NSW_TRAINS and others`() {
        val map = mapOf(
            NswTransportModeType.BUSES to listOf(gtfsStop("9", "Bus Stop 9")),
            NswTransportModeType.NSW_TRAINS to listOf(gtfsStop("9", "NSW Train Stop 9")),
            NswTransportModeType.SYDNEY_TRAINS to listOf(gtfsStop("9", "Sydney Train Stop 9")),
            NswTransportModeType.LIGHT_RAIL to listOf(gtfsStop("9", "Light Rail Stop 9"))
        )
        val result = createCommonGtfsStops(map)
        assertEquals(1, result.size)
        assertTrue(result[0].productClass.containsAll(
            listOf(
                NswTransportModeType.BUSES.productClass,
                NswTransportModeType.NSW_TRAINS.productClass,
                NswTransportModeType.SYDNEY_TRAINS.productClass,
                NswTransportModeType.LIGHT_RAIL.productClass
            )
        ))
        // Name should be from SYDNEY_TRAINS
        assertEquals("Sydney Train Stop 9", result[0].name)
    }
}