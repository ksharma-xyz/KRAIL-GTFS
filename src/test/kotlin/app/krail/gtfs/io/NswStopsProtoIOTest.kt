package app.krail.gtfs.io

import app.krail.kgtfs.io.NswStopsProtoIO
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.proto.KrailNswStopList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class NswStopsProtoIOTest {

    /**
     * Test writing and reading a Protobuf file for NSW Stops data.
     */
    @Test
    fun testWriteAndReadProtoFile() = runTest {
        val cacheDirPath = "test_cache"
        val filePath = "$cacheDirPath/NSW_STOPS.pb"
        val result = listOf(
            StopJson("1", "Stop 1", 1.0.toString(), 1.0.toString(), mutableSetOf(1, 5)),
            StopJson("2", "Stop 2", 2.0.toString(), 2.0.toString(), mutableSetOf(7)),
            StopJson("3", "Stop 3", 3.0.toString(), 3.0.toString(), mutableSetOf(1))
        )

        // Ensure the cache directory exists
        File(cacheDirPath).mkdirs()

        // Write the Protobuf file
        NswStopsProtoIO.writeProtoFile(result, filePath)

        // Read the Protobuf file
        val decoded: KrailNswStopList = NswStopsProtoIO.readProtoFile(filePath)

        // Verify the decoded data
        assertEquals(result.size, decoded.nswStops.size)
        assertEquals(result[0].id, decoded.nswStops[0].stopId)
        assertEquals(result[1].id, decoded.nswStops[1].stopId)

        // Clean up
        File(filePath).delete()
    }

    /**
     * This test will check if the data written to .pb file is actually being able to parse
     * correctly or not.
     */
    @Test
    fun testReadNSWStopsProtoFile() = runTest {
        val filePath = "nswstops/NSW_STOPS.pb"

        // Read the Protobuf file
        val decoded: KrailNswStopList = NswStopsProtoIO.readProtoFile(filePath)

        // Verify the total number of stops
        println("Total stops: ${decoded.nswStops.size}")
        // assertEquals(1, decoded.nswStops.size) // Don't know how many stops will be there
    }
}