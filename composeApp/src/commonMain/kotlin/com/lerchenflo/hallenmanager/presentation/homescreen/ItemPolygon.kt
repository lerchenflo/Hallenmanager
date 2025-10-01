package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lerchenflo.hallenmanager.domain.Item

@Composable
fun ItemPolygon(
    item: Item,
    scale: Float,
    offset: Offset,
    onClick: () -> Unit
) {
    // Calculate bounding box (world/item coordinates)
    val minX = item.cornerPoints.minOf { it.x }
    val minY = item.cornerPoints.minOf { it.y }
    val maxX = item.cornerPoints.maxOf { it.x }
    val maxY = item.cornerPoints.maxOf { it.y }

    val width = maxX - minX
    val height = maxY - minY

    // Transform to screen coordinates for Box placement
    val screenX = minX * scale + offset.x
    val screenY = minY * scale + offset.y
    val screenWidth = width * scale
    val screenHeight = height * scale

    Box(
        modifier = Modifier
            .offset { IntOffset(screenX.toInt(), screenY.toInt()) }
            .size(screenWidth.dp, screenHeight.dp)
    ) {
        // Keep current canvas size in pixels so pointerInput can compute relative points
        val canvasSizeState = remember { mutableStateOf(IntSize(0, 0)) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSizeState.value = it } // save current size (px)
                .pointerInput(item) {
                    detectTapGestures { tapOffset ->
                        // compute polygon points in canvas-local coordinates
                        val cs = canvasSizeState.value
                        if (cs.width == 0 || cs.height == 0 || width == 0f || height == 0f) return@detectTapGestures

                        val canvasW = cs.width.toFloat()
                        val canvasH = cs.height.toFloat()

                        val relativePoints = item.cornerPoints.map {
                            Offset(
                                ((it.x - minX) / width) * canvasW,
                                ((it.y - minY) / height) * canvasH
                            )
                        }

                        if (isPointInPolygon(tapOffset, relativePoints)) {
                            onClick()
                        }
                    }
                }
        ) {
            // Build path in the same way (use drawScope.size here)
            val path = Path().apply {
                fillType = PathFillType.NonZero

                // drawScope size corresponds to the same canvas-local coordinates we used above
                val relativePoints = item.cornerPoints.map {
                    Offset(
                        ((it.x - minX) / width) * size.width,
                        ((it.y - minY) / height) * size.height
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

            // Outline
            drawPath(
                path = path,
                color = item.getColor(),
                style = Stroke(width = 4f / scale * (screenWidth / size.width), cap = StrokeCap.Round)
            )

            // Fill
            drawPath(
                path = path,
                color = item.getColor().copy(alpha = 0.1f),
                style = Fill
            )
        }

        // Text overlays - as regular Compose Text
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val titleFontSp = (25f / scale).coerceIn(10f, 64f).sp
            val descFontSp = (17f / scale).coerceIn(8f, 48f).sp

            Text(
                text = item.title,
                color = item.getColor(),
                fontSize = titleFontSp,
                maxLines = 1
            )

            if (item.description.isNotBlank()) {
                Text(
                    text = item.description,
                    color = item.getColor().copy(alpha = 0.8f),
                    fontSize = descFontSp,
                    maxLines = 1
                )
            }
        }
    }
}

/** Ray-casting point-in-polygon test (works for convex & concave polygons). */
private fun isPointInPolygon(point: Offset, polygon: List<Offset>): Boolean {
    var inside = false
    val n = polygon.size
    if (n < 3) return false
    var j = n - 1
    for (i in 0 until n) {
        val xi = polygon[i].x
        val yi = polygon[i].y
        val xj = polygon[j].x
        val yj = polygon[j].y

        val intersect = ((yi > point.y) != (yj > point.y)) &&
                (point.x < (xj - xi) * (point.y - yi) / (yj - yi + 0.0f) + xi)
        if (intersect) inside = !inside
        j = i
    }
    return inside
}
