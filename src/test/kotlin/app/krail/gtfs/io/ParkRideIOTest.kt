package app.krail.gtfs.io

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.io.processParkRideData
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.parkride.StopIdParkRideMapping
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParkRideIOTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic("app.krail.kgtfs.io.FileStorageKt") // for writeJsonToFile
        mockkStatic("app.krail.kgtfs.network.ParkRideServiceKt") // for getCarParkFacilities
        mockkObject(app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `should log and return if API fails`() = runTest {
        coEvery { app.krail.kgtfs.network.ParkRideService.getCarParkFacilities() } returns Result.failure(Exception("API error"))
        coEvery { writeJsonToFile(any<List<StopIdParkRideMapping>>(), any(), any(), any()) } returns Unit
        val stops = listOf(StopJson("stop1", "name1", "0.0", "0.0", mutableSetOf(1)))
        processParkRideData(stops)
        coVerify(exactly = 0) { writeJsonToFile(any<List<StopIdParkRideMapping>>(), any(), any(), any()) }
    }
/*
    @Test
    fun `should handle empty stops`() = runTest {
        every { app.krail.kgtfs.network.ParkRideService.getCarParkFacilities() } returns Result.success(mapOf("f1" to "Park1"))
        every { writeJsonToFile(any(), any(), any(), any()) } just Runs
        app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings = listOf(
            StopIdParkRideMapping("stop1", "f1")
        )
        processParkRideData(emptyList())
        verify { writeJsonToFile(emptyList<Any>(), any(), any(), any()) }
    }

    @Test
    fun `should handle no mappings`() = runTest {
        every { app.krail.kgtfs.network.ParkRideService.getCarParkFacilities() } returns Result.success(mapOf("f1" to "Park1"))
        every { writeJsonToFile(any(), any(), any(), any()) } just Runs
        app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings = emptyList()
        processParkRideData(listOf(StopJson("stop1", "name1")))
        verify { writeJsonToFile(emptyList<Any>(), any(), any(), any()) }
    }

    @Test
    fun `should filter mappings by stopId and facilityId`() = runTest {
        every { app.krail.kgtfs.network.ParkRideService.getCarParkFacilities() } returns Result.success(
            mapOf(
                "f1" to "Park1",
                "f2" to "Park2"
            )
        )
        every { writeJsonToFile(any(), any(), any(), any()) } just Runs
        app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings = listOf(
            StopIdParkRideMapping("stop1", "f1"),
            StopIdParkRideMapping("stop2", "f2"),
            StopIdParkRideMapping("stop3", "f3") // not in API
        )
        val stops = listOf(StopJson("stop1", "name1"), StopJson("stop2", "name2"), StopJson("stop3", "name3"))
        processParkRideData(stops)
        val expected = listOf(
            StopIdParkRideMapping("stop1", "f1"),
            StopIdParkRideMapping("stop2", "f2")
        )
        verifySequence {
            writeJsonToFile(expected, any(), "NSW_PARKRIDE_PRETTY", true)
            writeJsonToFile(expected, any(), "NSW_PARKRIDE", false)
        }
    }

    @Test
    fun `should log unmapped facilityIds`() = runTest {
        every { app.krail.kgtfs.network.ParkRideService.getCarParkFacilities() } returns Result.success(
            mapOf(
                "f1" to "Park1",
                "f2" to "Park2"
            )
        )
        every { writeJsonToFile(any(), any(), any(), any()) } just Runs
        app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings = listOf(
            StopIdParkRideMapping("stop1", "f1")
        )
        val stops = listOf(StopJson("stop1", "name1"))
        processParkRideData(stops)
        val expected = listOf(StopIdParkRideMapping("stop1", "f1"))
        verifySequence {
            writeJsonToFile(expected, any(), "NSW_PARKRIDE_PRETTY", true)
            writeJsonToFile(expected, any(), "NSW_PARKRIDE", false)
        }
    }*/
}