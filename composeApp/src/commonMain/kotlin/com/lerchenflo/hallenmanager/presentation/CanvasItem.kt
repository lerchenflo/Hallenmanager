package com.lerchenflo.hallenmanager.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.nativeCanvas
import com.lerchenflo.hallenmanager.domain.Item


@Composable
fun CanvasItem(
    canvas: Canvas,
    item: Item
) {

    canvas.drawPoints(
        pointMode = PointMode.Points,
        points = item.cornerPoints,
        paint = Paint(

        )
    )
}