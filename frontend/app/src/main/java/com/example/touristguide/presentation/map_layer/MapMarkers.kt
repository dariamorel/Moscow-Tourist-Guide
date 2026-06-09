package com.example.touristguide.presentation.map_layer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.touristguide.R
import com.example.touristguide.domain.entities.Sight
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import kotlin.math.max

private const val NORMAL_MARKER_SIZE_DP = 48
private const val SELECTED_MARKER_SIZE_DP = 72

@Composable
fun MapMarkers(
    mapView: MapView,
    sights: List<Sight>,
    selectedSightId: Int? = null,
    onMapClick: () -> Unit = {},
    onMarkerClick: (Sight) -> Unit = {}
) {
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        val receiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                onMapClick()
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }

        val eventsOverlay = MapEventsOverlay(receiver)
        mapView.overlays.add(0, eventsOverlay)
    }

    LaunchedEffect(sights, selectedSightId) {
        mapView.overlays.removeAll {
            it is FolderOverlay
        }

        val markersOverlay = FolderOverlay()

        sights.forEach { sight ->
            val isSelected = sight.id == selectedSightId
            val markerSize = if (isSelected) SELECTED_MARKER_SIZE_DP else NORMAL_MARKER_SIZE_DP
            val markerDrawable = createMarkerDrawable(
                context = ctx,
                drawableResId = R.drawable.maps_icon,
                sizeDp = markerSize,
                label = if (isSelected) sight.title else null
            )

            val marker = Marker(mapView).apply {
                position = sight.coords
                title = sight.title
                icon = markerDrawable
                if (isSelected && markerDrawable != null) {
                    val iconHeightPx = (markerSize * ctx.resources.displayMetrics.density).toInt()
                    val anchorV = iconHeightPx.toFloat() / markerDrawable.intrinsicHeight
                    setAnchor(Marker.ANCHOR_CENTER, anchorV.coerceIn(0f, 1f))
                }
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(sight)
                    true
                }
            }
            markersOverlay.add(marker)
        }

        mapView.overlays.add(markersOverlay)
        mapView.invalidate()
    }
}

private fun createMarkerDrawable(
    context: Context,
    @DrawableRes drawableResId: Int,
    sizeDp: Int,
    label: String? = null
): Drawable? {
    val iconDrawable = resizeDrawable(context, drawableResId, sizeDp) ?: return null

    if (label.isNullOrBlank()) {
        return iconDrawable
    }

    val density = context.resources.displayMetrics.density
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11f * density
        color = android.graphics.Color.BLACK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    val maxLabelWidth = (120 * density).toInt()
    val displayLabel = ellipsizeLabel(label, paint, maxLabelWidth.toFloat())

    val iconWidth = iconDrawable.intrinsicWidth
    val iconHeight = iconDrawable.intrinsicHeight
    val textWidth = paint.measureText(displayLabel)
    val textHeight = paint.fontMetrics.let { it.descent - it.ascent }
    val padding = (4 * density).toInt()

    val totalWidth = max(iconWidth, textWidth.toInt()) + padding * 2
    val totalHeight = iconHeight + padding + textHeight.toInt()

    val bitmap = createBitmap(totalWidth, totalHeight)
    val canvas = Canvas(bitmap)

    val iconLeft = (totalWidth - iconWidth) / 2
    iconDrawable.setBounds(iconLeft, 0, iconLeft + iconWidth, iconHeight)
    iconDrawable.draw(canvas)

    canvas.drawText(
        displayLabel,
        totalWidth / 2f,
        iconHeight + padding - paint.fontMetrics.ascent,
        paint
    )

    return bitmap.toDrawable(context.resources)
}

private fun ellipsizeLabel(label: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(label) <= maxWidth) {
        return label
    }

    var truncated = label
    while (truncated.length > 1 && paint.measureText("$truncated…") > maxWidth) {
        truncated = truncated.dropLast(1)
    }
    return "$truncated…"
}

private fun resizeDrawable(context: Context, @DrawableRes drawableResId: Int, sizeDp: Int): Drawable? {
    val drawable = getDrawable(context, drawableResId) ?: return null

    val density = context.resources.displayMetrics.density
    val targetSizePx = (sizeDp * density).toInt()

    val originalWidth = drawable.intrinsicWidth
    val originalHeight = drawable.intrinsicHeight

    if (originalWidth <= 0 || originalHeight <= 0) {
        val bitmap = createBitmap(targetSizePx, targetSizePx)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, targetSizePx, targetSizePx)
        drawable.draw(canvas)
        return bitmap.toDrawable(context.resources)
    }

    val scale = targetSizePx.toFloat() / maxOf(originalWidth, originalHeight)

    val newWidth = (originalWidth * scale).toInt()
    val newHeight = (originalHeight * scale).toInt()

    val bitmap = createBitmap(newWidth, newHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap.toDrawable(context.resources)
}
