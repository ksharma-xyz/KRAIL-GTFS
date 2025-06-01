package app.krail.kgtfs.nsw.parkride

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StopIdParkRideMapping(
    @SerialName("stopId")
    val stopId: String,

    @SerialName("parkRideFacilityId")
    val parkRideFacilityId: String,

    @SerialName("parkRideName")
    val parkRideName: String,
)

// Source of truth for this data is https://opendata.transport.nsw.gov.au/data/dataset/car-park-api/resource/b880cae7-ed81-4d3e-9aba-b948d6626b20
val stopIdParkRideMappings = listOf(
    StopIdParkRideMapping(
        stopId = "207210",
        parkRideFacilityId = "6",
        parkRideName = "Park&Ride - Gordon Henry St (north)"
    ),
    StopIdParkRideMapping(stopId = "253330", parkRideFacilityId = "7", parkRideName = "Park&Ride - Kiama"),
    StopIdParkRideMapping(stopId = "225040", parkRideFacilityId = "8", parkRideName = "Park&Ride - Gosford"),
    StopIdParkRideMapping(stopId = "221210", parkRideFacilityId = "9", parkRideName = "Park&Ride - Revesby"),
    StopIdParkRideMapping(stopId = "210120", parkRideFacilityId = "10", parkRideName = "Park&Ride - Warriewood"),
    StopIdParkRideMapping(stopId = "210115", parkRideFacilityId = "11", parkRideName = "Park&Ride - Narrabeen"),
    StopIdParkRideMapping(stopId = "210318", parkRideFacilityId = "12", parkRideName = "Park&Ride - Mona Vale"),
    StopIdParkRideMapping(stopId = "209913", parkRideFacilityId = "13", parkRideName = "Park&Ride - Dee Why"),
    StopIdParkRideMapping(stopId = "211420", parkRideFacilityId = "14", parkRideName = "Park&Ride - West Ryde"),
    StopIdParkRideMapping(stopId = "223210", parkRideFacilityId = "15", parkRideName = "Park&Ride - Sutherland"),
    StopIdParkRideMapping(stopId = "2232126", parkRideFacilityId = "15", parkRideName = "Park&Ride - Sutherland"),
    StopIdParkRideMapping(stopId = "2232254", parkRideFacilityId = "15", parkRideName = "Park&Ride - Sutherland"),
    StopIdParkRideMapping(stopId = "217933", parkRideFacilityId = "16", parkRideName = "Park&Ride - Leppington"),
    StopIdParkRideMapping(
        stopId = "217426",
        parkRideFacilityId = "17",
        parkRideName = "Park&Ride - Edmondson Park (south)"
    ),
    StopIdParkRideMapping(stopId = "276010", parkRideFacilityId = "18", parkRideName = "Park&Ride - St Marys"),
    StopIdParkRideMapping(
        stopId = "256020",
        parkRideFacilityId = "19",
        parkRideName = "Park&Ride - Campbelltown Farrow Rd (north)"
    ),
    StopIdParkRideMapping(
        stopId = "256020",
        parkRideFacilityId = "20",
        parkRideName = "Park&Ride - Campbelltown Hurley St"
    ),
    StopIdParkRideMapping(
        stopId = "275010",
        parkRideFacilityId = "21",
        parkRideName = "Park&Ride - Penrith (at-grade)"
    ),
    StopIdParkRideMapping(
        stopId = "275010",
        parkRideFacilityId = "22",
        parkRideName = "Park&Ride - Penrith (multi-level)"
    ),
    StopIdParkRideMapping(stopId = "217010", parkRideFacilityId = "23", parkRideName = "Park&Ride - Warwick Farm"),
    StopIdParkRideMapping(stopId = "276220", parkRideFacilityId = "24", parkRideName = "Park&Ride - Schofields"),
    StopIdParkRideMapping(stopId = "207763", parkRideFacilityId = "25", parkRideName = "Park&Ride - Hornsby"),
    StopIdParkRideMapping(stopId = "2155384", parkRideFacilityId = "26", parkRideName = "Park&Ride - Tallawong P1"),
    StopIdParkRideMapping(stopId = "2155384", parkRideFacilityId = "27", parkRideName = "Park&Ride - Tallawong P2"),
    StopIdParkRideMapping(stopId = "2155384", parkRideFacilityId = "28", parkRideName = "Park&Ride - Tallawong P3"),
    StopIdParkRideMapping(
        stopId = "2155382",
        parkRideFacilityId = "29",
        parkRideName = "Park&Ride - Kellyville (north)"
    ),
    StopIdParkRideMapping(
        stopId = "2155382",
        parkRideFacilityId = "30",
        parkRideName = "Park&Ride - Kellyville (south)"
    ),
    StopIdParkRideMapping(stopId = "2153478", parkRideFacilityId = "31", parkRideName = "Park&Ride - Bella Vista"),
    StopIdParkRideMapping(stopId = "2154392", parkRideFacilityId = "32", parkRideName = "Park&Ride - Hills Showground"),
    StopIdParkRideMapping(stopId = "2126158", parkRideFacilityId = "33", parkRideName = "Park&Ride - Cherrybrook"),
    StopIdParkRideMapping(
        stopId = "207010",
        parkRideFacilityId = "34",
        parkRideName = "Park&Ride - Lindfield Village Green"
    ),
    StopIdParkRideMapping(stopId = "220910", parkRideFacilityId = "35", parkRideName = "Park&Ride - Beverly Hills"),
    StopIdParkRideMapping(stopId = "275020", parkRideFacilityId = "36", parkRideName = "Park&Ride - Emu Plains"),
    StopIdParkRideMapping(stopId = "221010", parkRideFacilityId = "37", parkRideName = "Park&Ride - Riverwood"),
    StopIdParkRideMapping(stopId = "213110", parkRideFacilityId = "486", parkRideName = "Park&Ride - Ashfield"),
    StopIdParkRideMapping(stopId = "221710", parkRideFacilityId = "487", parkRideName = "Park&Ride - Kogarah"),
    StopIdParkRideMapping(stopId = "214710", parkRideFacilityId = "488", parkRideName = "Park&Ride - Seven Hills"),
    StopIdParkRideMapping(stopId = "209325", parkRideFacilityId = "489", parkRideName = "Park&Ride - Manly Vale"),
    StopIdParkRideMapping(stopId = "209324", parkRideFacilityId = "489", parkRideName = "Park&Ride - Manly Vale"),
    StopIdParkRideMapping(stopId = "210017", parkRideFacilityId = "490", parkRideName = "Park&Ride - Brookvale"),
)
