# Track Datasets (KRAIL-BFF live trip tracking)

This repo also builds the datasets that power **live trip tracking** in
[KRAIL-BFF](https://github.com/ksharma-xyz/KRAIL-BFF) — separate from, and
additive to, the stops/routes pipeline that updates the KRAIL app.

## What gets built

| Artifact | Contents | Why |
|---|---|---|
| `track_stops.pb` | Platform-level stop directory, merged across modes: raw GTFS `stop_id`, name, `parent_station`, lat/lon. Includes `location_type` 0 (platforms/stops) **and** 1 (stations). | GTFS-Realtime reports **platform** ids for trains (e.g. `2000336` = "Central Station Platform 16"); the app's search dataset only carries parents + bus stops, so the BFF needs this to name every stop. |
| `shapes_<bundle>.pb` | Per-mode `shape_id → encoded polyline` (Google polyline, precision 5) plus `trip_id → shape_id` index. Deduped: thousands of trips share a handful of shapes. | The BFF returns the route line for the map on the first tracking poll, keyed by the realtime trip id. |
| `track_manifest.json` | `{version, generated_at, artifacts:[{name, url, sha256, size_bytes}]}` | The BFF fetches this, verifies sha256, and hot-swaps datasets in memory when `version` changes. |

Bundles: sydneytrains, nswtrains, metro, lightrail (consolidated), and
sydney ferries. **Buses are excluded** — bus shapes are 10–50× larger; the
BFF draws straight-line geometry for buses instead.

Schema: [`src/main/proto/TrackDataset.proto`](../src/main/proto/TrackDataset.proto).
The field numbers/types are a wire contract with the BFF's decoder
(`KRAIL-BFF/server/src/main/proto/nsw/track-dataset.proto`) — change both
together or not at all.

## How it runs

`.github/workflows/track-dataset.yml` — weekly cron (Sun 16:00 UTC) +
manual `workflow_dispatch`:

1. `./gradlew buildTrackDataset` downloads the GTFS bundles (using the
   `NSW_TRANSPORT_API_KEY` repo secret, same as the stops pipeline) and
   derives the artifacts into `cache/track/`.
2. The job recreates the **rolling `track-latest` GitHub Release** with the
   fresh artifacts. Nothing is committed to git — these are machine-derived
   binaries, regenerated weekly.

## How KRAIL-BFF stays up to date — no PRs needed

The BFF's `TRACK_DATASET_MANIFEST_URL` points at:

```
https://github.com/ksharma-xyz/KRAIL-GTFS/releases/download/track-latest/track_manifest.json
```

It re-checks the manifest every ~6 hours and hot-swaps in memory when the
version bumps. A failed or missing release degrades gracefully on the BFF
side (search-dataset stop names, straight-line map lines) — this workflow
can never break tracking, only un-enhance it.

## Local run

```bash
./gradlew buildTrackDataset            # needs NSW_TRANSPORT_API_KEY (env or local.properties)
ls cache/track                          # track_stops.pb, shapes_*.pb, track_manifest.json
```

Point a locally running BFF at the output with
`TRACK_DATASET_DIR=/path/to/KRAIL-GTFS/cache/track`.
