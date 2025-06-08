package app.krail.kgtfs.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.util.*

fun getHttpClient(): HttpClient {
    val apiKey = getApiKey()

    return HttpClient(OkHttp) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) {
                   // println(message)
                }
            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        defaultRequest {
            headers.append(
                HttpHeaders.Authorization,
                "apikey $apiKey"
            )
        }
    }
}

private fun getApiKey(): String {
    val apiKey = System.getenv("NSW_TRANSPORT_API_KEY")
    if (!apiKey.isNullOrEmpty()) {
        return apiKey
    }

    val properties = Properties()
    FileInputStream("local.properties").use { properties.load(it) }
    return properties.getProperty("NSW_TRANSPORT_API_KEY")
}
