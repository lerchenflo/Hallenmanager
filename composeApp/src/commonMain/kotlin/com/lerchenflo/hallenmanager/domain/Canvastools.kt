package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.roundToInt

fun snapToGrid(point: Offset, gridSpacing: Float): Offset {
    return Offset(
        x = (point.x / gridSpacing).roundToInt() * gridSpacing,
        y = (point.y / gridSpacing).roundToInt() * gridSpacing
    )
}

data class Line(var start: Offset = Offset.Zero, var end: Offset = Offset.Zero)
