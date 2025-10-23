package org.example

import kotlinx.coroutines.runBlocking

val tags = listOf(
    "amusement",
    "anticafe",
    "art-centers",
    "art-space",
    "attractions",
    "bar",
    "bridge",
    "business",
    "church",
    "cinema",
    "clubs",
    "comedy-club",
    "concert-hall",
    "culture",
    "fountain",
    "handmade",
    "homesteads",
    "library",
    "monastery",
    "museums",
    "observatory",
    "palace",
    "park",
    "photo-places",
    "prirodnyj-zapovednik",
    "questroom",
    "recreation",
    "restaurants",
    "shops",
    "sights",
    "station",
    "synagogue",
    "temple",
    "theatre",
    "workshops"
)

val fields = listOf(
    "id",
    "title",
    "address",
    "timetable",
    "phone",
    "images",
    "description",
    "body_text",
    "foreign_url",
    "coords",
    "subway",
    "categories",
)


fun main() = runBlocking {
    val sightsDownloader = SightsDownloader()
    sightsDownloader.downloadSightsByTags(
        tags = tags.joinToString(","),
        fields = fields.joinToString(",")
    )
}