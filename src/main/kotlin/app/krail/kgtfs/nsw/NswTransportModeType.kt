package app.krail.kgtfs.nsw


enum class NswTransportModeType(val modeName: String, val productClass: Int) {
    BUSES(modeName = "Buses", productClass = 5),
    NSW_TRAINS(modeName = "NswTrains", productClass = 1),
    LIGHT_RAIL(modeName = "LightRail", productClass = 4),
    SYDNEY_FERRY(modeName = "SydneyFerry", productClass = 9),
    SYDNEY_METRO(modeName = "SydneyMetro", productClass = 2),
    SYDNEY_TRAINS(modeName = "SydneyTrains", productClass = 1),
    COACH(modeName = "Coach", productClass = 7),

    LIGHT_RAIL_PARRAMATTA(modeName = "LightRailParramatta", productClass = 4),
    LIGHT_RAIL_INNERWEST(modeName = "LightRailInnerWest", productClass = 4),
    LIGHT_RAIL_NEWCASTLE(modeName = "LightRailNewcastle", productClass = 4),
    LIGHT_RAIL_CBD_AND_SOUTHEAST(modeName = "LightRailCbdAndSoutheast", productClass = 4),
    ;
}
