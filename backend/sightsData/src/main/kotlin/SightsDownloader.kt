package org.example

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.io.File

/**
 * Загрузчик списка достопримечательностей.
 */
class SightsDownloader {

    /**
     * Загрузить список достопримечательностей по тэгам.
     *
     * @param tags тэги
     * @param fields поля, которые должны присутствовать в ответе.
     */
    suspend fun downloadSightsByTags(tags: String = "", fields: String = "") {
        try {
            val client = HttpClient(CIO)
            val response: HttpResponse = client.get("$BASE_URL/v1.4/places/") {
                url {
                    parameters.append("categories", tags)
                    parameters.append("fields", fields)
                    parameters.append("location", "msk")
                    parameters.append("text_format", "text")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                val jsonString = response.bodyAsText()
                File(OUTPUT_FILE).writeText(jsonString)
            } else {
                throw RuntimeException("Response status is not OK.")
            }
        } catch (e: Exception) {
            throw RuntimeException("Error while downloading sights list: ${e.message}")
        }
    }

    companion object {
        const val BASE_URL = "https://kudago.com/public-api/"
        const val OUTPUT_FILE = "sights_list.json"
    }
}