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


    @Test
    fun `filterOutBusStandData merges stops and productClass correctly`() {
        val input = listOf(
            StopJson(
                id = "214732",
                name = "Seven Hills Station, Stand A",
                lat = "-33.77",
                lon = "150.93",
                productClass = mutableSetOf(1)
            ),
            StopJson(
                id = "214733",
                name = "Seven Hills Station, Stand B",
                lat = "-33.77",
                lon = "150.93",
                productClass = mutableSetOf(1)
            ),
            StopJson(
                id = "214710",
                name = "Seven Hills Station",
                lat = "-33.77",
                lon = "150.93",
                productClass = mutableSetOf(5)
            ),
        )

        val result = filterOutBusStandData(input)

        assertEquals(1, result.size)

        val sevenHills = result.find { it.name == "Seven Hills Station" }!!
        assertEquals(setOf(1, 5), sevenHills.productClass)
        assertEquals("214710", sevenHills.id)
    }

    @Test
    fun `preserveChildren returns parent plus all child stops with isParent flags`() {
        val input = listOf(
            StopJson("2148424", "Westpoint Bus Interchange, Stand 10", "-33.770416", "150.906243", mutableSetOf(5)),
            StopJson("2148425", "Westpoint Bus Interchange, Stand 9", "-33.770591", "150.906186", mutableSetOf(5)),
            StopJson("2148426", "Westpoint Bus Interchange, Stand 8", "-33.770751", "150.906136", mutableSetOf(5)),
        )

        val result = filterOutBusStandData(input, preserveChildren = true)

        // Should have 1 parent + 3 children = 4 total
        assertEquals(4, result.size)

        val parent = result.find { it.isParent == null }
        val children = result.filter { it.isParent == false }

        // Verify parent
        assertEquals("Westpoint Bus Interchange", parent?.name)
        assertEquals(null, parent?.isParent)  // null = default true

        // Verify children
        assertEquals(3, children.size)
        assertEquals(true, children.all { it.isParent == false })
        assertEquals(true, children.all { it.name.contains("Stand") })
        assertEquals(setOf("2148424", "2148425", "2148426"), children.map { it.id }.toSet())
    }
}
