package com.example.touristguide.presentation.map_layer

import android.content.Context
import android.graphics.Canvas
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

@Composable
fun MapMarkers(
    mapView: MapView,
    sights: List<Sight>,
    onMapClick: () -> Unit = {},
    onMarkerClick: (Sight) -> Unit = {}
) {
    val markersOverlay = FolderOverlay()
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

    LaunchedEffect(sights) {

        mapView.overlays.removeAll {
            it is FolderOverlay
        }

        val markersOverlay = FolderOverlay()

        sights.forEach { sight ->
            val marker = Marker(mapView).apply {
                position = sight.coords
                title = sight.title
                icon = resizeDrawable(
                    ctx,
                    R.drawable.maps_icon,
                    48
                )
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