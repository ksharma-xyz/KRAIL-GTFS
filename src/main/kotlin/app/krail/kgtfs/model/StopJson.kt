package app.krail.kgtfs.model

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StopJson(
    val id: String,
    var name: String,
    val lat: String,
    val lon: String,
    val productClass: MutableSet<Int>,
    @EncodeDefault(EncodeDefault.Mode.NEVER)
    val isParent: Boolean? = null  // null (default) = parent; false = child
)
