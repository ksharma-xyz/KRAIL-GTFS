package app.krail.kgtfs.model

import kotlinx.serialization.Serializable

@Serializable
data class StopJson(
    val id: String,
    var name: String,
    val lat: String,
    val lon: String,
    val productClass: MutableSet<Int>
)
