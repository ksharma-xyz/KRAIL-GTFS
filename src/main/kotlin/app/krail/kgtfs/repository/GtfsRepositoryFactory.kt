package app.krail.kgtfs.repository

import app.krail.kgtfs.nsw.NswTransportModeType

/**
 * Factory for creating GtfsRepository instances.
 *
 * Centralizes repository creation logic and allows for easy swapping
 * of implementations (e.g., in-memory vs database-backed).
 */
object GtfsRepositoryFactory {

    /**
     * Create a new GTFS repository instance.
     *
     * @param cacheDirectory Base directory where GTFS files are cached
     * @param modeName Name of the transport mode subdirectory (e.g., "Buses")
     * @param nswTransportModeType Transport mode type enum
     * @return Configured GtfsRepository instance
     */
    fun create(
        cacheDirectory: String,
        modeName: String,
        nswTransportModeType: NswTransportModeType
    ): GtfsRepository {
        return InMemoryGtfsRepository(
            cacheDirectory = cacheDirectory,
            modeName = modeName,
            nswTransportModeType = nswTransportModeType
        )
    }

    /**
     * Create a repository using the transport mode's default configuration.
     *
     * @param cacheDirectory Base directory where GTFS files are cached
     * @param nswTransportModeType Transport mode type (uses its modeName property)
     * @return Configured GtfsRepository instance
     */
    fun create(
        cacheDirectory: String,
        nswTransportModeType: NswTransportModeType
    ): GtfsRepository {
        return create(
            cacheDirectory = cacheDirectory,
            modeName = nswTransportModeType.modeName,
            nswTransportModeType = nswTransportModeType
        )
    }
}

