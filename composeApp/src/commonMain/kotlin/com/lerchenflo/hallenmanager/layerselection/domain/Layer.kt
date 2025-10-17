package com.lerchenflo.hallenmanager.layerselection.domain

import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto

data class Layer(
    val layerid: Long,
    val name: String,
    val sortId: Int,
    val shown: Boolean,
    val color: Long,
    var serverId: Long?

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
    color = color,
    serverId = serverId
)

fun LayerDto.toLayer(): Layer = Layer(
    layerid = layerid,
    name = name,
    sortId = sortId,
    shown = shown,
    color = color,
    serverId = serverId
)