package com.example.touristguide.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.touristguide.data.dto.AchievementDto
import com.example.touristguide.data.dto.RouteDto
import com.example.touristguide.data.dto.RouteUpdateInputDto
import com.example.touristguide.data.dto.SightDto
import com.example.touristguide.domain.entities.Achievement
import com.example.touristguide.domain.entities.Route
import com.example.touristguide.domain.entities.Sight
import org.osmdroid.util.GeoPoint
import java.time.OffsetDateTime

fun SightDto.toDomain(): Sight {
    return Sight(
        id = id,
        title = title,
        image = img,
        description = shortDesc,
        address = addr,
        timetable = timetable,
        phone = phone,
        bodyText = desc,
        coords = GeoPoint(lat, lon),
        subway = subway,
        categories = tags.orEmpty()
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun RouteDto.toDomain(): Route {
    val plannedSights = plannedOrder.orEmpty().map { it.toDomain() }
    val passedIds = order.orEmpty().toSet()

    return Route(
        id = id,
        name = name.orEmpty(),
        isPassed = plannedSights.associate { sight ->
            sight.id to (sight.id in passedIds)
        },
        planned = plannedSights,
        totalTime = totalTime,
        date = runCatching { OffsetDateTime.parse(date) }.getOrNull(),
        status = status.toRouteStatus()
    )
}

fun Route.toUpdateDto(): RouteUpdateInputDto {
    val passedOrder = planned
        .map { it.id }
        .filter { isPassed[it] == true }

    return RouteUpdateInputDto(
        name = name,
        order = passedOrder,
        status = status.toApiStatus()
    )
}

fun AchievementDto.toDomain(): Achievement {
    return Achievement(
        id = id,
        type = type.toAchievementType(),
        name = name,
        description = description,
        iconUrl = iconUrl,
        requiredCount = requiredCount,
        progress = progress,
        tag = tag.orEmpty()
    )
}

private fun String?.toRouteStatus(): Route.Status {
    return when (this) {
        "in_progress" -> Route.Status.IN_PROGRESS
        "completed" -> Route.Status.PASSED
        "declined" -> Route.Status.DECLINED
        else -> Route.Status.NOT_STARTED
    }
}

private fun Route.Status.toApiStatus(): String {
    return when (this) {
        Route.Status.IN_PROGRESS -> "in_progress"
        Route.Status.PASSED -> "completed"
        Route.Status.DECLINED -> "declined"
        Route.Status.NOT_STARTED -> "planned"
    }
}

private fun String.toAchievementType(): Achievement.Type {
    return when (this) {
        "sights_visited" -> Achievement.Type.SIGHTS_VISITED
        "routes_completed" -> Achievement.Type.ROUTES_COMPLETED
        else -> Achievement.Type.TAG_COLLECTOR
    }
}
