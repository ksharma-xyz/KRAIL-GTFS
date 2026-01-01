# Data Formats Reference

## Overview

KRAIL-GTFS processes NSW GTFS data and exports it in multiple formats optimized for different use cases. All generated files are stored in the `cache/` directory.

---

## Generated Files

After running `./gradlew runKRAIL-GTFS`, the following files are created:

```
cache/
├── NSW_STOPS.json                  # Compact JSON (all stops)
├── NSW_STOPS_PRETTY.json           # Human-readable JSON
├── NSW_STOPS.pb                    # Protobuf (mobile app)
├── NSW_PARKRIDE.json               # Park & Ride facilities
├── NSW_PARKRIDE_PRETTY.json        # Human-readable
├── NSW_BUSES_ROUTES.json           # Compact JSON (route mappings)
├── NSW_BUSES_ROUTES_PRETTY.json    # Human-readable JSON
└── NSW_BUSES_ROUTES.pb             # Protobuf (mobile app)
```

---

## Stops Data Format

### Purpose
Contains all NSW transport stops with coordinates and transport mode associations.

### Files
- `NSW_STOPS.pb` - **Used by mobile app** (protobuf, smallest)
- `NSW_STOPS.json` - Compact JSON for APIs
- `NSW_STOPS_PRETTY.json` - Human-readable for debugging

### Protobuf Schema (`KrailStop.proto`)

```protobuf
message NswStop {
  string stopId = 1;              // Unique stop identifier
  string stopName = 2;            // "Central Station"
  double lat = 3;                 // -33.8831
  double lon = 4;                 // 151.2061
  repeated int32 productClass = 5; // [1, 2, 5] (train, metro, bus)
}

message NswStopList {
  repeated NswStop nswStops = 1;
}
```

### JSON Example
```json
[
  {
    "id": "10101100",
    "name": "Central Station",
    "lat": -33.8831,
    "lon": 151.2061,
    "productClass": [1, 2, 5]
  }
]
```

---

## Routes Data Format

### Purpose
Maps bus route numbers to their serviced stops, handling route variants and directions.

### Files
- `NSW_BUSES_ROUTES.pb` - **Used by mobile app** (protobuf, structured)
- `NSW_BUSES_ROUTES.json` - Compact JSON
- `NSW_BUSES_ROUTES_PRETTY.json` - Human-readable JSON

### Protobuf Schema (`NswBusRoute.proto`)

```protobuf
message NswBusRouteList {
  string transportMode = 1;
  string generatedAt = 2;
  repeated NswBusRouteGroup routes = 3;
}

message NswBusRouteGroup {
  string routeShortName = 1; // "702"
  repeated NswBusRouteVariant variants = 2;
}

message NswBusRouteVariant {
  string routeId = 1;
  string routeName = 2;
  repeated NswBusTripOption trips = 3;
}

message NswBusTripOption {
  string tripId = 1;
  string headsign = 2;
  repeated string stopIds = 3;
}
```

### JSON Example
```json
{
  "transport_mode": "Buses",
  "generated_at": "2026-01-01T12:00:00Z",
  "routes": {
    "702": [
      {
        "route_id": "2504_702",
        "route_name": "Blacktown to Seven Hills",
        "trips": [
          {
            "trip_id": "2303543",
            "headsign": "Blacktown to Seven Hills",
            "stop_ids": ["214818", "214820", ...]
          }
        ]
      }
    ]
  }
}
```

---

## Park & Ride Data Format

### Purpose
Lists all park & ride facilities with their associated transit stops.

### Files
- `NSW_PARKRIDE.json` - Compact JSON
- `NSW_PARKRIDE_PRETTY.json` - Human-readable

### JSON Example
```json
[
  {
    "stopId": "10101100",
    "parkRideFacilityId": "24",
    "parkRideName": "Park&Ride - Central",
    "latitude": -33.8831,
    "longitude": 151.2061
  }
]
```

---

## File Sizes

| File | Format | Size | Use Case |
|------|--------|------|----------|
| NSW_STOPS.pb | Protobuf | ~2-3 MB | Mobile app (production) |
| NSW_STOPS.json | JSON | ~5-6 MB | APIs, testing |
| NSW_BUSES_ROUTES.pb | Protobuf | ~400-600 KB | Mobile app (production) |
| NSW_BUSES_ROUTES.json | JSON | ~1 MB | APIs, testing |

**Why Protobuf for mobile?**
- 50% smaller than JSON
- Faster parsing
- Type-safe schema

