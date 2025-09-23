package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.data.LayerDto

data class Layer(
    val layerid: Long,
    val name: String,
    val sortId: Int,
    var shown: Boolean,
    val color: Long
)


fun Layer.toLayerDto(): LayerDto = LayerDto(
    layerid = layerid,
    name = name,
    sortId = sortId,
    shown = shown,
    color = color
)

fun LayerDto.toLayer(): Layer = Layer(
    layerid = layerid,
    name = name,
    sortId = sortId,
    shown = shown,
    color = color
)