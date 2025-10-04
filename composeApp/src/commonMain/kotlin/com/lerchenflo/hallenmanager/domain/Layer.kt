package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.data.LayerDto

data class Layer(
    val layerid: Long,
    val name: String,
    val sortId: Int,
    val shown: Boolean,
    val color: Long
){
    fun getColor(): Color {
        //println("Layercolor: ${Color(color.toULong())}")
        return Color(color.toULong())
    }
}


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