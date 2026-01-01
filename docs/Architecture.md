# GTFS Route-to-Stops Mapping

## Goal

**Build a map from bus route numbers (e.g., "303") to their serviced stops using NSW GTFS data.**

Export as minimal JSON (route_number → [stop_ids]) for mobile app consumption.

---

## GTFS Data Structure

### Input Files

| File | Purpose | Key Fields |
|------|---------|------------|
| `routes.txt` | Route metadata | `route_id`, `route_short_name` (e.g., "303") |
| `trips.txt` | Trip patterns per route | `trip_id`, `route_id`, `direction_id` |
| `stop_times.txt` | Stops per trip in sequence | `trip_id`, `stop_id`, `stop_sequence` |
| `stops.txt` | Stop details | `stop_id`, `stop_name`, `stop_lat`, `stop_lon` |

### Relationships

```
routes.txt       trips.txt       stop_times.txt       stops.txt
┌─────────┐     ┌─────────┐     ┌─────────────┐     ┌─────────┐
│route_id │────>│trip_id  │────>│trip_id      │     │stop_id  │
│route_   │     │route_id │     │stop_id      │────>│stop_name│
│short_   │     │direction│     │stop_sequence│     │lat, lon │
│name     │     │         │     │             │     │         │
└─────────┘     └─────────┘     └─────────────┘     └─────────┘
```

---

## Core Algorithm

```
function buildStructuredRouteData(cacheDirectory, modeName, nswTransportModeType):
    // 1. Read all GTFS files
    stops = readGtfsStops(...)
    routes = readGtfsRoutes(...)
    trips = readGtfsTrips(...)
    stopTimes = readGtfsStopTimes(...)
    
    // 2. Group routes by short name (e.g. "702")
    routesByShortName = routes.groupBy { it.routeShortName }
    
    // 3. Build structured hierarchy
    structuredData = {}
    
    for each (shortName, variants) in routesByShortName:
        routeVariants = []
        
        for each variant in variants:
            // Find trips for this specific route_id (e.g. "2504_702")
            tripsForVariant = trips.filter { it.routeId == variant.routeId }
            
            // Group by direction/headsign
            tripsByDirection = tripsForVariant.groupBy { it.headsign }
            
            tripOptions = []
            for each (headsign, trips) in tripsByDirection:
                // Pick representative trip (longest sequence)
                representativeTrip = trips.maxBy { it.stopCount }
                stopIds = getStopsForTrip(representativeTrip)
                
                tripOptions.add({
                    trip_id: representativeTrip.tripId,
                    headsign: headsign,
                    stop_ids: stopIds
                })
            
            routeVariants.add({
                route_id: variant.routeId,
                route_name: variant.routeLongName,
                trips: tripOptions
            })
            
        structuredData[shortName] = routeVariants
        
    return structuredData
```

**Key Points:**
- **Solves Route Collisions:** Distinguishes between different routes sharing the same number (e.g. 702 in Sydney vs Newcastle)
- **Direction Aware:** Provides specific stop lists for each direction/headsign
- **Representative Trips:** Selects the most complete trip pattern for static display

---

## Architecture

### Repository Pattern (Lazy Loading + Caching)

```
┌─────────────────────────────────────────────┐
│         GtfsRepositoryFactory               │
│  create(cacheDir, mode, transportType)      │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│       InMemoryGtfsRepository                │
│  - Lazy initialization (on first access)   │
│  - Thread-safe with Mutex                   │
│  - Builds indices once, caches results      │
│                                             │
│  API:                                       │
│  + getStructuredRouteData()                 │
│  + getAllRoutes()                           │
│  + getStats()                               │
└──────────────────┬──────────────────────────┘
```

### JSON Export (Structured Format)

```kotlin
// Output: cache/NSW_BUSES_ROUTES.json
{
  "transport_mode": "Buses",
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

**Why Structured:**
- Enables disambiguation of routes with same short name
- Provides direction-specific stop lists (TripView style)
- Optimized for mobile app navigation flow

---

## Implementation Files

### Core Logic
- **`app.krail.kgtfs.repository.GtfsRepository`** - Interface defining API
- **`app.krail.kgtfs.repository.InMemoryGtfsRepository`** - Implementation with lazy loading
- **`app.krail.kgtfs.repository.GtfsRepositoryFactory`** - Factory for creating repositories

### Data Models
- **`app.krail.kgtfs.model.GtfsRoute`** - Route data class
- **`app.krail.kgtfs.model.GtfsTrip`** - Trip data class
- **`app.krail.kgtfs.model.StructuredRouteData`** - Structured export model

### File I/O
- **`app.krail.kgtfs.csv.CsvReader`** - Reads GTFS CSV files
- **`app.krail.kgtfs.io.StructuredRouteIO`** - Exports to JSON and Protobuf
- **`app.krail.kgtfs.io.FileStorage`** - Generic JSON writing utility

### Entry Point
- **`app.krail.kgtfs.Main`** - Runs GTFS fetch, processing, and export

---

## Key Design Decisions

✅ **In-Memory Only** - No database, fast lookups  
✅ **Lazy Initialization** - Load data on first access  
✅ **Thread-Safe** - Mutex-protected initialization  
✅ **Minimal Export** - Stop IDs only (97% size reduction)  
✅ **Direction Support** - Can filter by inbound/outbound  
✅ **Protobuf Export** - ~50% smaller than JSON, faster parsing  
✅ **Centralized Constants** - `AppConstants` for all paths and file names  
✅ **Parallel Processing** - Stops and routes processed concurrently  
✅ **CI/CD Integration** - Auto-generates and uploads .pb files to KRAIL app
