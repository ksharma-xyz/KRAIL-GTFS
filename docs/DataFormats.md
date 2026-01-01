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
Maps route numbers to ordered stop IDs. Mobile app has stop details locally, so only IDs are needed.

### Files
- `NSW_BUSES_ROUTES.pb` - **Used by mobile app** (protobuf, smallest)
- `NSW_BUSES_ROUTES.json` - Compact JSON
- `NSW_BUSES_ROUTES_PRETTY.json` - Human-readable

### Protobuf Schema (`KrailRoute.proto`)

```protobuf
message KrailRoute {
  string routeNumber = 1;          // "303", "M50", "T1"
  repeated string stopIds = 2;     // Ordered stop IDs
}

message KrailRouteList {
  string transportMode = 1;        // "Buses"
  int32 totalRoutes = 2;          // 4702
  string generatedAt = 3;          // "2026-01-01T12:00:00Z"
  repeated KrailRoute routes = 4;
}
```

### JSON Example
```json
{
  "transport_mode": "Buses",
  "total_routes": 4702,
  "generated_at": "2026-01-01T12:00:00Z",
  "routes": {
    "303": ["2031186", "203256", "203323"],
    "M50": ["209512", "209513", "209514"]
  }
}
```

**Note:** Array position = stop sequence (index 0 = first stop)

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

