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
function buildRouteToStopsMap(cacheDirectory, modeName, nswTransportModeType):
    // 1. Read all GTFS files
    stops = readGtfsStops("$cacheDirectory/$modeName/stops.txt")
    routes = readGtfsRoutes("$cacheDirectory/$modeName/routes.txt")
    trips = readGtfsTrips("$cacheDirectory/$modeName/trips.txt")
    stopTimes = readGtfsStopTimes("$cacheDirectory/$modeName/stop_times.txt")
    
    // 2. Build lookup indices
    stopById = stops.associateBy { it.stopId }
    routeIdToShortName = routes.associate { route_id -> route_short_name }
    tripsByRoute = trips.groupBy { it.routeId }
    stopTimesByTrip = stopTimes.groupBy { it.tripId }
    
    // 3. Aggregate stops per route (across all trip variants)
    routeToStopsMap = {}
    
    for each (routeId, tripsForRoute) in tripsByRoute:
        routeShortName = routeIdToShortName[routeId]
        allStopsForRoute = LinkedHashSet() // preserves order, removes duplicates
        
        for each trip in tripsForRoute:
            stopsForTrip = stopTimesByTrip[trip.tripId]
                .sortedBy { it.stopSequence }
                .map { stopById[it.stopId] }
            
            allStopsForRoute.addAll(stopsForTrip)
        
        routeToStopsMap[routeShortName] = allStopsForRoute.toList()
    
    return routeToStopsMap
```

**Key Points:**
- Merges all trip variants (inbound/outbound/peak/off-peak) into single stop list per route
- Uses `LinkedHashSet` to deduplicate while preserving order
- Pre-builds indices for O(1) lookups instead of O(n) filtering

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
│  + getStopsByRoute(routeNum)                │
│  + getStopsByRouteAndDirection(num, dir)    │
│  + getAllRoutes()                           │
│  + getStats()                               │
└──────────────────┬──────────────────────────┘
                   │
                   v
┌─────────────────────────────────────────────┐
│          Cached Indices (In-Memory)         │
│  - routeToStopsMap: Map<String, List<Stop>> │
│  - routeDirectionToStops: Map<Key, List>    │
│  - stopById: Map<StopId, GtfsStop>          │
└─────────────────────────────────────────────┘
```

### JSON Export (Minimal Format)

```kotlin
// Output: cache/NSW_BUSES_ROUTES.json
{
  "transport_mode": "Buses",
  "total_routes": 4702,
  "generated_at": "2026-01-01T12:00:00Z",
  "routes": {
    "303": ["2031186", "203256", "203323", ...],  // only stop IDs
    "M50": ["209512", "209513", ...]               // 97% size reduction
  }
}
```

**Why Minimal:**
- Mobile app already has stop details (name, lat, lon) in local DB
- Only need stop IDs in sequence
- Array position = sequence number (no redundant field)

---

## Implementation Files

### Core Logic
- **`app.krail.kgtfs.repository.GtfsRepository`** - Interface defining API
- **`app.krail.kgtfs.repository.InMemoryGtfsRepository`** - Implementation with lazy loading
- **`app.krail.kgtfs.repository.GtfsRepositoryFactory`** - Factory for creating repositories

### Data Models
- **`app.krail.kgtfs.model.GtfsRoute`** - Route data class
- **`app.krail.kgtfs.model.GtfsTrip`** - Trip data class
- **`app.krail.kgtfs.model.GtfsStopTime`** - Stop time data class
- **`app.krail.kgtfs.model.RouteStopsJson`** - Minimal JSON export model

### File I/O
- **`app.krail.kgtfs.csv.CsvReader`** - Reads GTFS CSV files
- **`app.krail.kgtfs.io.RouteStopsJsonIO`** - Exports to JSON
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
