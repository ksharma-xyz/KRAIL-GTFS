package app.krail.kgtfs.track

import java.io.File

/**
 * Minimal, LENIENT RFC 4180-ish CSV parser for the track dataset builder:
 * handles quoted fields, escaped quotes (""), optional UTF-8 BOM, and the
 * malformed quoting NSW ships in some bundles (e.g. nswtrains stops.txt has
 * stray characters after a closing quote, which strict parsers reject).
 * Returns one Map per row keyed by the header row.
 */
internal object GtfsCsv {

    fun readWithHeader(file: File): List<Map<String, String>> {
        val rows = parse(file.readText())
        val header = rows.firstOrNull() ?: return emptyList()
        return rows.drop(1).map { row ->
            buildMap {
                header.forEachIndexed { i, key -> put(key, row.getOrNull(i).orEmpty()) }
            }
        }
    }

    fun parse(input: String): List<List<String>> {
        val source = if (input.isNotEmpty() && input[0] == '﻿') input.substring(1) else input
        val rows = mutableListOf<List<String>>()
        val current = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < source.length) {
            val c = source[i]
            when {
                inQuotes -> when {
                    c == '"' && i + 1 < source.length && source[i + 1] == '"' -> {
                        field.append('"'); i++
                    }
                    c == '"' -> inQuotes = false
                    else -> field.append(c)
                }
                c == '"' -> inQuotes = true
                c == ',' -> {
                    current.add(field.toString()); field.setLength(0)
                }
                c == '\n' -> {
                    current.add(field.toString()); field.setLength(0)
                    rows.add(current.toList()); current.clear()
                }
                c == '\r' -> { /* swallow; treat as part of CRLF */ }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || current.isNotEmpty()) {
            current.add(field.toString())
            rows.add(current.toList())
        }
        return rows
    }
}
