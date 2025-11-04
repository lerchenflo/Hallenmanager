package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.lerchenflo.hallenmanager.mainscreen.domain.Item

@Composable
fun ItemPolygon(
    item: Item,
    scale: Float,
    targetSize: DpSize? = null,
    offset: Offset? = null,
    showTitle: Boolean = true
) {
    // Calculate bounding box in CONTENT coordinates (pixels)
    val minX = item.cornerPoints.minOf { it.offsetX }
    val minY = item.cornerPoints.minOf { it.offsetY }
    val maxX = item.cornerPoints.maxOf { it.offsetX }
    val maxY = item.cornerPoints.maxOf { it.offsetY }

    val width = maxX - minX
    val height = maxY - minY

    // Convert pixels to dp for Compose
    val density = LocalDensity.current
    val widthDp = with(density) { targetSize?.width ?: width.toDp() }
    val heightDp = with(density) { targetSize?.height ?: height.toDp() }

    // Position the Box in CONTENT space using pixel offset
    Box(
        modifier = Modifier
            .then(
                if (targetSize == null) {
                    if (offset == null){
                        Modifier.offset { IntOffset(minX.toInt(), minY.toInt()) }
                    }else {
                        Modifier.offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
                    }
                } else {
                    Modifier
                }
            )
            .size(widthDp, heightDp)
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Build path using RELATIVE coordinates within the bounding box
            val path = Path().apply {
                fillType = PathFillType.NonZero

                val relativePoints = item.cornerPoints.map {
                    Offset(
                        ((it.offsetX - minX) / width) * size.width,
                        ((it.offsetY - minY) / height) * size.height
                    )
                }

                if (relativePoints.isNotEmpty()) {
                    moveTo(relativePoints[0].x, relativePoints[0].y)
                    for (i in 1 until relativePoints.size) {
                        lineTo(relativePoints[i].x, relativePoints[i].y)
                    }
                    close()
                }
            }

            // Outline - stroke width adjusted for scale
            drawPath(
                path = path,
                color = item.getAbsoluteColor(),
                style = Stroke(width = 4f / scale, cap = StrokeCap.Round)
            )

            // Fill
            drawPath(
                path = path,
                color = item.getAbsoluteColor().copy(alpha = 0.2f),
                style = Fill
            )
        }

        if (showTitle){
            // Text overlays
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val titleFontSp = (25f / scale).coerceIn(10f, 20f).sp
                val descFontSp = (17f / scale).coerceIn(8f, 18f).sp

                Text(
                    text = item.title,
                    color = item.getAbsoluteColor(),
                    fontSize = titleFontSp,
                    maxLines = 1
                )

                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        color = item.getAbsoluteColor().copy(alpha = 0.8f),
                        fontSize = descFontSp,
                        maxLines = 1
                    )
                }
            }
        }

    }
}

fun isPointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    if (polygon.size < 3) return false

    val cleanPolygon = if (polygon.first() == polygon.last() && polygon.size > 3) {
        polygon.dropLast(1)
    } else {
        polygon
    }

    if (cleanPolygon.size < 3) return false

    var inside = false
    var j = cleanPolygon.size - 1

    for (i in cleanPolygon.indices) {
        val xi = cleanPolygon[i].x
        val yi = cleanPolygon[i].y
        val xj = cleanPolygon[j].x
        val yj = cleanPolygon[j].y

        val cond1 = (yi > point.y) != (yj > point.y)

        if (cond1) {
            val xIntersect = (xj - xi) * (point.y - yi) / (yj - yi) + xi
            if (point.x < xIntersect) {
                inside = !inside
            }
        }
        j = i
    }

    return inside
}