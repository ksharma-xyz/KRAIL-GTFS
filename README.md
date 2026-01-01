# GTFS Data for KRAIL Appt

[![CI Workflow](https://github.com/ksharma-xyz/KRAIL-GTFS/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ksharma-xyz/KRAIL-GTFS/actions/workflows/ci.yml)

This repository contains a Kotlin project designed to process GTFS (General Transit Feed Specification) data to generate
a list of stops in JSON and Protobuf format. The GTFS stops.txt file is parsed into a StopJson object. This stops data is converted into a 
protobuf binary / json format data which is used to power stop search functionality in the KRAIL App

This data is designed to **support the KRAIL app**, which can be found at the following
repository: [KRAIL GitHub Repository](https://github.com/ksharma-xyz/Krail). 

## Features

- Parse and process GTFS stop data.
- Convert transit stop data into JSON and protobuf files.
- Export both compact and pretty-printed JSON formats with automated file naming.
- Efficient file I/O operations using coroutines.
- **Automated KRAIL App Updates**: Automatically creates PRs in the KRAIL repository with updated `.pb` files.

---

## Automated Workflows

This repository includes GitHub Actions workflows that automatically:

### 1. Update GTFS Data (Every 5 days)
- Downloads latest GTFS data from NSW Transport API
- Generates JSON and Protobuf files
- Creates a PR in **this repository** with updated data files
- Auto-merges the PR after validation passes

### 2. Update KRAIL App Repository
When GTFS data is updated (via schedule or manual trigger), the workflow also:
- Generates the latest `NSW_STOPS.pb` file
- Checks out the [KRAIL repository](https://github.com/ksharma-xyz/Krail)
- Copies the `.pb` file to `io/gtfs/src/commonMain/composeResources/files/NSW_STOPS.pb`
- **Automatically bumps** the `NSW_STOPS_VERSION` constant in `SandookPreferences.kt` to trigger data refresh in the app
- Creates a PR in the **KRAIL repository** with both changes
- Enables auto-merge on the PR

This means the KRAIL app stays automatically synchronized with the latest transit data! 🚀

### Manual Trigger
You can manually trigger the workflow from the [Actions tab](https://github.com/ksharma-xyz/KRAIL-GTFS/actions/workflows/ci.yml) if you need to update the data immediately.

### GitHub Actions Architecture
The workflows are organized following best practices with reusable components:
- **Composite Actions** - Modular, reusable action logic
- **Reusable Workflows** - Sharable workflow templates
- **Clean Separation** - Main CI workflow orchestrates everything

📖 For detailed information about the workflow architecture, see [.github/ACTIONS_ARCHITECTURE.md](.github/ACTIONS_ARCHITECTURE.md)

---

## Getting Started

### Prerequisites

- **Kotlin SDK** version `2.1` or later.
- Java Development Kit (**JDK**) version `21`.
- Build system: **Gradle (Kotlin DSL)** or **Maven**.
- (Optional) **kotlinx.serialization** library for seamless JSON conversion.

### Installation

1. Clone the repository:
    ```bash
    git clone <repository-url>
    cd <repository-folder>
    ```
2. Make sure the required dependencies are available in your `build.gradle.kts`:
    ```kotlin
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0") // Serialization library
    }
    ```
3. Sync the project and ensure all configurations are applied.

### Development

If you modify the `.proto` files in `src/main/proto/`, you need to regenerate the Kotlin classes:

```bash
./gradlew generateMainProtos
```

---

## Usage

The primary functionality revolves around processing GTFS stop data and serializing it to JSON. Below are the steps to
use the main functions.

### Example: Writing Stop Data to JSON Files

Here’s how you can use the provided functions:

```kotlin
// Input: A map of GTFS stops categorized by transport mode
val gtfsStopMap: Map<NswTransportModeType, List<GtfsStop>> = ...

// Call the processing function
writeStopData(createCommonGtfsStops(gtfsStopMap))
```

This will:

1. Serialize the stop data (result of `createCommonGtfsStops`) to JSON format.
2. Automatically save it to the filesystem with both compact (`NSW_STOPS.json`) and pretty (`NSW_STOPS_PRETTY.json`)
   formats.

### Outputs

The JSON files are written to the specified directory (`cacheDirectory`) with proper naming conventions:

- `NSW_STOPS.json`: Compact JSON data.
- `NSW_STOPS_PRETTY.json`: Pretty-printed JSON data for readability.
- `NSW_STOPS.pb`: The protobuf binary data for Stops.
- `NSW_BUSES_ROUTES.json`: Compact JSON data for bus routes (structured).
- `NSW_BUSES_ROUTES_PRETTY.json`: Pretty-printed JSON data for bus routes.
- `NSW_BUSES_ROUTES.pb`: The protobuf binary data for bus routes.

---

## Project Structure

- **`createCommonGtfsStops()`**: Processes GTFS stop data into a unified `List<StopJson>` structure.
- **`writeJsonToFile()`**: A generic helper function to write any Kotlin object to JSON files (supports pretty
  formatting).
- **`writeStopData()`**: A high-level function that coordinates the serialization for GTFS stop data, using
  `writeJsonToFile`.

---

## Development

### Building

To build the project, use Gradle:

```bash
./gradlew build
```

### Running Tests

To run all tests:

```bash
./gradlew test
```

---

## Contributing

Welcome contributions to improve this project! Feel free to open issues or submit pull requests.

---

## License

This project is licensed under the Apache License. See `LICENSE` file for more details.

---

## Contact

If you have any questions or feedback, feel free to raise an issue or reach out to the maintainers.

Email: hey@krail.app
