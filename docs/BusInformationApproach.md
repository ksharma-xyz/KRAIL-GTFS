# GTFS Route-to-Stops Mapping Implementation Plan

## Overview

Building a map from bus route numbers (e.g., "303") to their serviced stops using NSW GTFS data.

---

## Approach: Algorithm Walkthrough

```
function buildRouteToStopsMap(cacheDirectory, modeName, nswTransportModeType):
    // 1. Read all required files
    stops = readGtfsStops("$cacheDirectory/$modeName/stops.txt")
    routes = readGtfsRoutes("$cacheDirectory/$modeName/routes.txt")
    trips = readGtfsTrips("$cacheDirectory/$modeName/trips.txt")
    stopTimes = readGtfsStopTimes("$cacheDirectory/$modeName/stop_times.txt")
    
    // 2. Create lookup maps
    stopById = stops.associateBy { it.stopId }
    routeIdToShortName = routes.associateBy { route_id -> route_short_name }
    
    // 3. Group trips by route_id
    tripsByRoute = trips.groupBy { it.routeId }
    
    // 4. For each route, aggregate stops across all trips
    routeToStopsMap = empty map
    
    for each (routeId, tripsForRoute) in tripsByRoute:
        routeShortName = routeIdToShortName[routeId] ?: routeId
        allStopsForRoute = LinkedHashSet() // preserves order, removes duplicates
        
        for each trip in tripsForRoute:
            stopsForTrip = stopTimes
                .filter { it.tripId == trip.tripId }
                .sortedBy { it.stopSequence }
                .map { stopById[it.stopId] }
                .filterNotNull()
            
            allStopsForRoute.addAll(stopsForTrip)
        
        routeToStopsMap[routeShortName] = allStopsForRoute.toList()
    
    return routeToStopsMap
```

---

## Key Challenges

### 1. **Memory Management (OOM Risk)**

- **Problem**: `stop_times.txt` can contain millions of rows for city-wide transit
- **Impact**: Loading entire file into memory may cause OutOfMemoryError
- **Mitigation**: Streaming, chunking, and efficient data structures

### 2. **Route Variants & Directions**

- **Problem**: Route "303" may have multiple trip patterns (inbound/outbound, peak/off-peak)
- **Current Approach**: Merges all variants → union of all stops
- **Alternative**: Group by `(route_short_name, direction_id)` or `trip_headsign`

### 3. **Stop Deduplication**

- **Problem**: Same stop appears in multiple trips
- **Solution**: Use `LinkedHashSet` to preserve insertion order while removing duplicates

### 4. **Missing/Orphaned Data**

- **Problem**: `stop_times.txt` may reference stops not in `stops.txt` or trips not in `trips.txt`
- **Current Handling**: Silent filtering with `filterNotNull()`
- **Improvement**: Log warnings and track parse errors

### 5. **Performance**

- **Problem**: Nested loops and repeated filtering can be inefficient
- **Solution**: Pre-build indices, use appropriate data structures

---

## Scalability Considerations

### Architecture Improvements

1. **Repository Pattern**
    - Abstract GTFS data access
    - Enable caching and lazy loading
    - Swap implementations without changing business logic

2. **Lazy Computation**
    - Don't build entire map upfront
    - Compute route→stops on-demand
    - Cache results using `ConcurrentHashMap`

3. **Streaming & Chunking**
    - Process `stop_times.txt` in batches
    - Don't load entire file at once
    - Use Kotlin sequences for lazy evaluation

4. **Index-Based Lookups**
   ```
   tripIdToRouteId: Map<TripId, RouteId>
   tripIdToStopIds: Map<TripId, List<StopId>>  // sorted by sequence
   stopIdToGtfsStop: Map<StopId, GtfsStop>
   ```

5. **Memory-Efficient Data Structures**
    - Use primitive collections where possible (e.g., `IntArrayList` for sequences)
    - Intern common strings (route IDs, stop IDs)
    - Consider weak references for cached data

---

## Implementation Roadmap

### Phase 1: Setup & Refactoring (Foundation)

- [ ] **1.1 Code Organization**
    - Create dedicated package: `app.krail.kgtfs.repository`
    - Separate concerns: CSV reading vs domain logic
    - Move GTFS-specific readers to `GtfsFileReaders.kt`

- [ ] **1.2 Domain Models**
    - Define clear data classes: `GtfsRoute`, `GtfsTrip`, `GtfsStopTime`
    - Add validation in constructors
    - Implement `equals()`/`hashCode()` properly for deduplication

- [ ] **1.3 Error Handling**
    - Create `ParseResult<T>` sealed class (Success/Error)
    - Add `ParseContext` for tracking warnings/errors
    - Implement structured logging

- [ ] **1.4 Testing Infrastructure**
    - Set up test data (small GTFS sample)
    - Create unit test structure
    - Add memory profiling tests

---

### Phase 2: Core API Design (Interface)

- [ ] **2.1 Define Repository Interface**
  ```kotlin
  interface GtfsRepository {
      suspend fun getStopsByRoute(routeShortName: String): Result<List<GtfsStop>>
      suspend fun getStopsByRouteAndDirection(
          routeShortName: String, 
          directionId: Int
      ): Result<List<GtfsStop>>
      suspend fun getAllRoutes(): Result<List<String>>
  }
  ```

- [ ] **2.2 Implement In-Memory Repository**
    - `InMemoryGtfsRepository` with lazy initialization
    - Build indices on first access
    - Thread-safe caching with `ConcurrentHashMap`

- [ ] **2.3 Factory/Builder Pattern**
  ```kotlin
  class GtfsRepositoryFactory {
      fun create(
          cacheDirectory: Path,
          modeName: String,
          nswTransportModeType: NswTransportModeType
      ): GtfsRepository
  }
  ```

---

### Phase 3: Efficient Implementation (Performance)

- [ ] **3.1 Streaming File Readers**
    - Modify CSV readers to use Kotlin `Sequence`
    - Process `stop_times.txt` in batches (e.g., 10,000 rows)
    - Emit progress/logging for large files

- [ ] **3.2 Index Building Strategy**
  ```kotlin
  // Build indices incrementally
  private fun buildIndices() {
      val tripToRoute = mutableMapOf<String, String>()
      val tripToStops = mutableMapOf<String, MutableList<StopTimeEntry>>()
      
      // Stream stop_times and group by trip_id
      readGtfsStopTimesSequence().forEach { stopTime ->
          tripToStops.getOrPut(stopTime.tripId) { mutableListOf() }.add(stopTime)
      }
      
      // Sort each trip's stops by sequence
      tripToStops.values.forEach { it.sortBy { st -> st.stopSequence } }
  }
  ```

- [ ] **3.3 Memory Optimization**
    - Use primitive collections (Eclipse Collections, Trove)
    - String interning for IDs: `String.intern()`
    - Clear intermediate data after index building
    - Add JVM memory flags documentation (`-Xmx`, `-XX:MaxMetaspaceSize`)

- [ ] **3.4 Algorithm Optimization**
    - Pre-compute `route_id → trip_ids` map
    - Avoid nested filtering: use indices
    - Use `LinkedHashSet` for deduplication with order preservation

---

### Phase 4: Testing & Validation

- [ ] **4.1 Unit Tests**
    - Test each file reader independently
    - Test index building logic
    - Test route-to-stops mapping with known data

- [ ] **4.2 Integration Tests**
    - Test with real NSW GTFS dataset (small sample)
    - Validate results against NSW Transport API (if available)
    - Test edge cases: missing files, malformed data

- [ ] **4.3 Performance Tests**
    - Benchmark memory usage with real dataset
    - Measure time for index building
    - Profile CPU hotspots with VisualVM/YourKit
    - Test with different JVM heap sizes

- [ ] **4.4 Load Testing**
    - Simulate concurrent requests
    - Test cache eviction strategies
    - Verify no memory leaks with repeated queries

---

### Phase 5: Production Readiness

- [ ] **5.1 Documentation**
    - API documentation (KDoc)
    - Usage examples
    - Performance characteristics
    - Memory requirements

- [ ] **5.2 Configuration**
    - Externalize batch sizes
    - Configurable cache sizes
    - Log level configuration

- [ ] **5.3 Monitoring**
    - Add metrics (cache hit rate, query time)
    - Memory usage tracking
    - Error rate monitoring

---

## File Dependencies (GTFS Spec)

| File             | Purpose                    | Key Fields                                     |
|------------------|----------------------------|------------------------------------------------|
| `routes.txt`     | Route metadata             | `route_id`, `route_short_name` (bus number)    |
| `trips.txt`      | Trip patterns per route    | `trip_id`, `route_id`, `direction_id`          |
| `stop_times.txt` | Stops per trip in sequence | `trip_id`, `stop_id`, `stop_sequence`          |
| `stops.txt`      | Stop details               | `stop_id`, `stop_name`, `stop_lat`, `stop_lon` |

---

## Success Criteria

- [ ] Successfully maps all route numbers to their stops
- [ ] Handles NSW GTFS dataset without OOM errors
- [ ] Query performance < 100ms for cached routes
- [ ] < 500ms for uncached routes (first access)
- [ ] Memory usage < 2GB for complete NSW dataset
- [ ] 95%+ code coverage on core logic
- [ ] Zero data loss (all stops accounted for)

---

## Notes

- **No Database**: Solution uses in-memory data structures only
- **Concurrency**: Repository must be thread-safe for concurrent queries
- **Updates**: Consider how to handle GTFS data updates (reload strategy)
- **Direction Handling**: Future enhancement to separate inbound/outbound stops