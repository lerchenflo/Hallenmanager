package com.lerchenflo.hallenmanager.data

import androidx.compose.ui.geometry.Offset
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class Item(
    val title: String,
    val description: String,
    val layer: String,
    val lastchanged: String = Clock.System.now().toEpochMilliseconds().toString(),
    val created : String = Clock.System.now().toEpochMilliseconds().toString(),

    val cornerPoints: List<Offset>
) {
}