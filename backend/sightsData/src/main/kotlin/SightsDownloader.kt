package org.example

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Загрузчик списка достопримечательностей.
 */
class SightsDownloader {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Загрузить список достопримечательностей по тэгам.
     *
     * @param tags тэги
     * @param fields поля, которые должны присутствовать в ответе.
     */
    suspend fun downloadSightsByTags(tags: String, fields: String) {
        val client = HttpClient(CIO)
        val allSights = mutableListOf<Sight>()

        var nextUrl: String? = "$BASE_URL/v1.4/places/?categories=$tags&fields=$fields&location=msk&text_format=text"

        try {
            while (nextUrl != null) {
                println("Downloading page: $nextUrl")
                val response = client.get(nextUrl!!)

                if (response.status == HttpStatusCode.OK) {
                    val jsonString = response.bodyAsText()
                    val pageResponse = json.decodeFromString<SightsResponse>(jsonString)

                    allSights.addAll(pageResponse.results)
                    println("Loaded ${pageResponse.results.size} items. Total loaded: ${allSights.size}")

                    nextUrl = pageResponse.next
                } else {
                    println("Failed to download page. Status: ${response.status}")
                    break
                }
            }

            if (allSights.isNotEmpty()) {
                val resultJson = json.encodeToString(allSights)
                File(OUTPUT_FILE).writeText(resultJson)
                println("Successfully saved ${allSights.size} sights to $OUTPUT_FILE")
            } else {
                println("No sights found.")
            }

        } catch (e: Exception) {
            println("Error while downloading sights list: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
        }
    }

    companion object {
        const val BASE_URL = "https://kudago.com/public-api"
        const val OUTPUT_FILE = "sights_list.json"
    }
}
