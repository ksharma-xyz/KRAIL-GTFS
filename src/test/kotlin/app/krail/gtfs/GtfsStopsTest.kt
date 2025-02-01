package app.krail.gtfs

import app.krail.kgtfs.model.GtfsStop
import app.krail.kgtfs.model.StopId
import app.krail.kgtfs.nsw.NswGtfsManager.createCommonGtfsStops
import app.krail.kgtfs.nsw.NswTransportModeType
import kotlin.test.Test
import kotlin.test.assertEquals

class GtfsStopsTest {

    private val busGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("2"), name = "Stop 2", latitude = 2.0, longitude = 2.0),
        GtfsStop(stopId = StopId("22"), name = "Stop 22", latitude = 2.0, longitude = 2.0)
    )

    private val trainGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("3"), name = "Stop 3", latitude = 2.0, longitude = 2.0),
        GtfsStop(stopId = StopId("5"), name = "Stop 5", latitude = 2.0, longitude = 2.0),
    )

    private val nswTrainGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("4"), name = "Stop 4", latitude = 2.0, longitude = 2.0),
    )

    private val lightRailGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("2"), name = "Stop 2", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("5"), name = "Stop 5", latitude = 2.0, longitude = 2.0),
        GtfsStop(stopId = StopId("6"), name = "Stop 6", latitude = 2.0, longitude = 2.0),
    )

    private val ferryGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("6"), name = "Stop 6", latitude = 2.0, longitude = 2.0),
    )

    private val metroGtfsStopList = listOf(
        GtfsStop(stopId = StopId("1"), name = "Stop 1", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("2"), name = "Stop 2", latitude = 1.0, longitude = 1.0),
        GtfsStop(stopId = StopId("7"), name = "Stop 7", latitude = 2.0, longitude = 2.0),
    )

    /**
     * Test the creation of a common list of stops from multiple GTFS sources.
     * The stops should be merged and the product classes should be combined for same stopIds.
     */
    @Test
    fun testCreateCommonGtfsStopsWithOverlap() {
        val gtfsStopMap = mapOf(
            NswTransportModeType.BUSES to busGtfsStopList,
            NswTransportModeType.SYDNEY_TRAINS to trainGtfsStopList,
            NswTransportModeType.SYDNEY_METRO to metroGtfsStopList,
            NswTransportModeType.LIGHT_RAIL to lightRailGtfsStopList,
            NswTransportModeType.NSW_TRAINS to nswTrainGtfsStopList,
            NswTransportModeType.SYDNEY_FERRY to ferryGtfsStopList
        )

        val result = createCommonGtfsStops(gtfsStopMap)

        result.find { it.id == "1" }?.productClass?.let {
            assertEquals(setOf(5, 1, 2, 4, 9), it)
        }

        result.find { it.id == "2" }?.productClass?.let {
            assertEquals(setOf(5,  2, 4), it)
        }

        result.find { it.id == "3" }?.productClass?.let {
            assertEquals(setOf(1), it)
        }

        result.find { it.id == "4" }?.productClass?.let {
            assertEquals(setOf(1), it)
        }

        result.find { it.id == "5" }?.productClass?.let {
            assertEquals(setOf(1, 4), it)
        }

        result.find { it.id == "6" }?.productClass?.let {
            assertEquals(setOf(4, 9), it)
        }

        result.find { it.id == "7" }?.productClass?.let {
            assertEquals(setOf(2), it)
        }

        result.find { it.id == "22" }?.productClass?.let {
            assertEquals(setOf(5), it)
        }
    }
}