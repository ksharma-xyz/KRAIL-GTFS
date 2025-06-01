package app.krail.kgtfs.io

import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.network.ParkRideService.getCarParkFacilities
import app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath

suspend fun processParkRideData(stops: List<StopJson>) = withContext(Dispatchers.IO) {
    // 1. Call the API to get the facility map
    val facilityResult = getCarParkFacilities()
    if (facilityResult.isFailure) {
        println("Failed to fetch car park facilities: ${facilityResult.exceptionOrNull()}")
        return@withContext
    }
    val facilityMap = facilityResult.getOrThrow() // facilityId -> parkRideName

    // 2. Filter mappings to only those whose stopId is present in the stops list
    val stopIds = stops.map { it.id }.toSet()
    val filteredMappings = stopIdParkRideMappings.filter { it.stopId in stopIds }

    // 3. Map facilityId to StopIdParkRideMapping if possible
    val mapped = filteredMappings.filter { mapping ->
        facilityMap.containsKey(mapping.parkRideFacilityId)
    }

    // 4. Log facilityIds from API that are not mapped
    val mappedFacilityIds = mapped.map { it.parkRideFacilityId }.toSet()
    val unmappedFacilityIds = facilityMap.keys.filter { it !in mappedFacilityIds }
    if (unmappedFacilityIds.isNotEmpty()) {
        println("Facility IDs from API not mapped to any stopId:")
        unmappedFacilityIds.forEach { id ->
            val name = facilityMap[id]
            println("  - $id: $name")
        }
    }

    // 5. Write mapped data to JSON files
    writeJsonToFile(
        data = mapped,
        path = cacheDirPath,
        fileName = "NSW_PARKRIDE_PRETTY",
        pretty = true,
    )
    writeJsonToFile(
        data = mapped,
        path = cacheDirPath,
        fileName = "NSW_PARKRIDE",
        pretty = false,
    )
}
