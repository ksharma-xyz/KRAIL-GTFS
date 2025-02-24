package app.krail.gtfs.filter

import app.krail.kgtfs.filter.BusStopsFilter.filterOutBusStandData
import app.krail.kgtfs.model.StopJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BusStopFilterTest {

    @Test
    fun testFilterOutBusStandData() {
        val input = listOf(
            StopJson("1", "Central Station, Railway Square, Stand L", "1.0", "1.0", mutableSetOf(1)),
            StopJson("2", "Central Station, Railway Square, Stand K", "1.0", "1.0", mutableSetOf(2)),
            StopJson("3", "Town Hall, Stand A", "2.0", "2.0", mutableSetOf(3)),
            StopJson("4", "Town Hall, Stand B", "2.0", "2.0", mutableSetOf(4)),
            StopJson("5", "Museum Station", "3.0", "3.0", mutableSetOf(5)),
            StopJson("6", "Central Station, Chalmers St, Stand G", "6.0", "6.0", mutableSetOf(5)),
            StopJson("7", "Macarthur Square, Kellicar Rd, Stand 1/2", "7.0", "7.0", mutableSetOf(5)),
            StopJson("8", "Macarthur Square, Kellicar Rd, Stand 4", "8.0", "8.0", mutableSetOf(5)),
            StopJson("9", "Merrylands Interchange, Terminal Pl - Stand 2", "8.0", "8.0", mutableSetOf(5)),
            StopJson("10", "Westpoint Bus Interchange, Stand 10", "10.0", "10.0", mutableSetOf(5)),
        )

        val expected = listOf(
            StopJson("1", "Central Station, Railway Square", "1.0", "1.0", mutableSetOf(1, 2)),
            StopJson("3", "Town Hall", "2.0", "2.0", mutableSetOf(3, 4)),
            StopJson("5", "Museum Station", "3.0", "3.0", mutableSetOf(5)),
            StopJson("5", "Central Station, Chalmers St", "6.0", "6.0", mutableSetOf(5)),
            StopJson("7", "Macarthur Square, Kellicar Rd", "7.0", "7.0", mutableSetOf(5)),
            StopJson("9", "Merrylands Interchange, Terminal Pl", "8.0", "8.0", mutableSetOf(5)),
            StopJson("10", "Westpoint Bus Interchange", "10.0", "10.0", mutableSetOf(5)),
        )

        val result = filterOutBusStandData(input)

        assertEquals(expected.size, result.size)
        for (i in expected.indices) {
            assertEquals(expected[i].name, result[i].name)
            assertEquals(expected[i].productClass, result[i].productClass)
        }
    }
}
