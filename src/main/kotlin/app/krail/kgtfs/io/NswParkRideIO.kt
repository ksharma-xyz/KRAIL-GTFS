package app.krail.kgtfs.io

import app.krail.kgtfs.AppConstants
import app.krail.kgtfs.io.FileStorage.writeJsonToFile
import app.krail.kgtfs.model.StopJson
import app.krail.kgtfs.nsw.parkride.StopIdParkRideMapping
import app.krail.kgtfs.nsw.parkride.stopIdParkRideMappings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Writes the park & ride data to JSON files.
 *
 * This function filters the provided list of stops to only include those that have a corresponding
 * park & ride mapping, and then writes the filtered mappings to two JSON files: one pretty-printed
 * and one compact.
 *
 * @param result The list of [StopJson] objects to filter and write.
 */
suspend fun writeParkRideData(
    result: List<StopJson>,
    staticMappings: List<StopIdParkRideMapping> = loadStaticParkRideMappings(),
) = withContext(Dispatchers.IO) {
    val stopIds = result.map { it.id }.toSet()
    val filteredMappings = stopIdParkRideMappings.filter { it.stopId in stopIds }
    val mergedMappings = (staticMappings + filteredMappings)
        .distinctBy { it.stopId to it.parkRideFacilityId }
        .sortedBy { it.parkRideName }

    writeJsonToFile(
        data = mergedMappings,
        path = AppConstants.CACHE_DIR_PATH,
        fileName = AppConstants.OutputFiles.NSW_PARK_RIDE,
        pretty = true,
    )
    writeJsonToFile(
        data = mergedMappings,
        path = AppConstants.CACHE_DIR_PATH,
        fileName = AppConstants.OutputFiles.NSW_PARK_RIDE,
        pretty = false,
    )
}

/**
 * Adding some mapping data these stopId's either represent an old StopID which was wrong or a new StopID is now available
 * therefore, we need to support those saved trips which have these StopID's for Park and Ride to display in the app automatically.
 */
fun loadStaticParkRideMappings(): List<StopIdParkRideMapping> = listOf(
    StopIdParkRideMapping("2762106", "24", "Park&Ride - Schofields"),
    StopIdParkRideMapping("207720", "25", "Park&Ride - Hornsby"),
    StopIdParkRideMapping("207763", "25", "Park&Ride - Hornsby"),
    StopIdParkRideMapping("275075", "21", "Park&Ride - Penrith (at-grade)"),
    StopIdParkRideMapping("275075", "22", "Park&Ride - Penrith (multi-level)"),
    StopIdParkRideMapping("214732", "488", "Park&Ride - Seven Hills"),
    StopIdParkRideMapping("2103108", "12", "Park&Ride - Mona Vale"),
    StopIdParkRideMapping("225041", "8", "Park&Ride - Gosford")
)