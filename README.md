# GTFS Stops Processor

This repository contains a Kotlin project designed to process GTFS (General Transit Feed Specification) data to generate
a list of stops in JSON format. It includes functionality to create stop data and serialize it into both compact and
pretty-printed JSON files. The project emphasizes ease of use, efficient memory handling, and modularity.

This data is designed to **support the KRAIL app**, which can be found at the following
repository: [KRAIL GitHub Repository](https://github.com/ksharma-xyz/Krail). The JSON output generated here will be used
within the app **KRAIL**.

## Features

- Parse and process GTFS stop data.
- Convert transit stop data into JSON files.
- Export both compact and pretty-printed JSON formats with automated file naming.
- Efficient file I/O operations using coroutines (`Dispatchers.IO`).

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
