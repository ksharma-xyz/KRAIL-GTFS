# Bus Route Data Logic

This document explains the structure of NSW Bus GTFS data and demonstrates how to trace a route from its definition to its stops. This manual process mirrors the logic implemented in the application and helps in understanding how the data is generated.

## Understanding the Data Relationships

The GTFS data for buses is structured as follows:

1.  **Routes (`routes.txt`)**: Defines the route (e.g., "702").
2.  **Trips (`trips.txt`)**: Defines specific trips for a route. A route can have multiple trips (different directions, variants).
3.  **Stop Times (`stop_times.txt`)**: Links trips to stops, defining the sequence and timing.
4.  **Stops (`stops.txt`)**: Defines the physical stop locations.

## Example: Tracing Route 702

The following steps demonstrate how to manually trace a specific bus route (e.g., "702") through the GTFS files. This validates the logic used by the `RouteProcessor` to generate the structured JSON output.

**Step 1:** 
Find the route_id for bus "702". Note that there are likely multiple "702" buses (e.g., one in Newcastle, one in Western Sydney).

`grep "\"702\" cache/Buses/routes.txt`

```
route_id,agency_id,route_short_name,route_long_name,route_desc,route_type,route_color,route_text_color
"2504_702","2504","702","Blacktown to Seven Hills","Sydney Buses Network","700","00B5EF","FFFFFF"
"3000_702","3000","702","Birmingham Gardens to Shortland Primary","School Buses","712","00B5EF","FFFFFF"
```

**Step 2:** 
Pick one route_id and find its trips Replace YOUR_ROUTE_ID with one found above (e.g., 244_700).

`grep "3000_702" cache/Buses/trips.txt | head -n 5`

```
route_id,service_id,trip_id,shape_id,trip_headsign,direction_id,block_id,wheelchair_accessible,trip_note,route_direction
"3000_702","2","2303543","184335","Shortland PS","1","","1","","Birmingham Gardens to Shortland Public School"
"3000_702","2","2303683","184407","Birmingham Gdns","0","","1","","Our Lady Of Victories Primary School to Birmingham Gardens"
```

**Step 3**
Step 3: See the stops for that trip Replace YOUR_TRIP_ID with the ID from Step 2.

`grep "2303543" cache/Buses/stop_times.txt | head -n 20`

```
trip_id,arrival_time,departure_time,stop_id,stop_sequence,stop_headsign,pickup_type,drop_off_type,shape_dist_traveled,timepoint,stop_note
"2303543","08:29:00","08:29:00","2287168","1","","0","0","0","1",""
"2303543","08:29:40","08:29:40","2287166","2","","0","0","307","0",""
"2303543","08:30:19","08:30:19","2287164","3","","0","0","597","0",""
"2303543","08:31:28","08:31:28","2287161","4","","0","0","1106","0",""
"2303543","08:32:27","08:32:27","230732","5","","0","0","1546","0",""
"2303543","08:33:06","08:33:06","230733","6","","0","0","1842","0",""
"2303543","08:34:53","08:34:53","230713","7","","0","0","2628","0",""
"2303543","08:35:30","08:35:30","230714","8","","0","0","2902","0",""
"2303543","08:36:18","08:36:18","230715","9","","0","0","3256","0",""
"2303543","08:36:54","08:36:54","230716","10","","0","0","3532","0",""
"2303543","08:38:03","08:38:03","230717","11","","0","0","4033","0",""
"2303543","08:38:38","08:38:38","230718","12","","0","0","4296","0",""
"2303543","08:39:21","08:39:21","230719","13","","0","0","4614","0",""
"2303543","08:40:03","08:40:03","230720","14","","0","0","4933","0",""
"2303543","08:40:37","08:40:37","230721","15","","0","0","5192","0",""
"2303543","08:43:58","08:43:58","230722","16","","0","0","6632","0",""
"2303543","08:44:36","08:44:36","230723","17","","0","0","6911","0",""
"2303543","08:45:09","08:45:09","230724","18","","0","0","7156","0",""
"2303543","08:46:00","08:46:00","230725","19","","0","0","7524","1",""
"2303543","08:46:32","08:46:32","230726","20","","0","0","7841","0",""
```

`grep "2303683" cache/Buses/stop_times.txt | head -n 20`

```
"2303683","15:02:00","15:02:00","230731","1","","0","0","0","1",""
"2303683","15:02:06","15:02:06","230732","2","","0","0","114","0",""
"2303683","15:02:24","15:02:24","230733","3","","0","0","410","0",""
"2303683","15:03:00","15:03:00","230728","4","","0","0","951","1",""
"2303683","15:03:49","15:03:49","230713","5","","0","0","1208","0",""
"2303683","15:04:42","15:04:42","230714","6","","0","0","1482","0",""
"2303683","15:05:51","15:05:51","230715","7","","0","0","1836","0",""
"2303683","15:06:44","15:06:44","230716","8","","0","0","2109","0",""
"2303683","15:08:24","15:08:24","230717","9","","0","0","2611","0",""
"2303683","15:09:14","15:09:14","230718","10","","0","0","2873","0",""
"2303683","15:10:17","15:10:17","230719","11","","0","0","3191","0",""
"2303683","15:11:18","15:11:18","230720","12","","0","0","3507","0",""
"2303683","15:12:09","15:12:09","230721","13","","0","0","3766","0",""
"2303683","15:17:00","15:17:00","230722","14","","0","0","5207","0",""
"2303683","15:17:56","15:17:56","230723","15","","0","0","5485","0",""
"2303683","15:18:44","15:18:44","230724","16","","0","0","5730","0",""
"2303683","15:19:57","15:19:57","230725","17","","0","0","6093","0",""
"2303683","15:21:00","15:21:00","230726","18","","0","0","6410","1",""
"2303683","15:21:20","15:21:20","230727","19","","0","0","6618","0",""
"2303683","15:21:40","15:21:40","230729","20","","0","0","6821","0",""
```

**Step 4**

Stop IDs from 4th column.

## Bus Routes have multiple Trip ids. Why?

For a given bus route number (e.g., "700"), there may be multiple trips representing different directions:
- Trip 1: "Blacktown to Parramatta" (44 stops)
- Trip 2: "Parramatta to Blacktown" (43 stops)

Reverse-direction trips for the same route **likely have different stops** because:
1. Bus stops are direction-specific (opposite sides of the road)
2. Stop order is reversed
3. Routes may take different paths in each direction (one-way streets, different return routes)
4. Stop IDs in GTFS are unique per physical location

**Example from route 700:**
- Blacktown→Parramatta: starts at `2148311`, ends at `2150114`
- Parramatta→Blacktown: starts at `2150107`, ends at `2148451`

These are **different stop IDs**, indicating different physical locations (opposite sides of streets).
