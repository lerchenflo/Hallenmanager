package com.lerchenflo.hallenmanager.layerselection.domain

import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto

data class Layer(
    val layerid: String,
    val name: String,
    val sortId: Int,
    val shown: Boolean,
    val color: Long,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
    var networkConnectionId: Long?

){
    fun getColor(): Color {
        //println("Layercolor: ${Color(color.toULong())}")
        return Color(color.toULong())
    }

    fun isRemoteLayer() : Boolean {
        return networkConnectionId != null
    }
}


fun Layer.toLayerDto(): LayerDto = LayerDto(
    layerid = layerid,
    name = name,
    sortId = sortId,
    shown = shown,
    color = color,
    networkConnectionId = networkConnectionId,
    createdAt = createdAt,
    lastchangedAt = lastchangedAt,
    lastchangedBy = lastchangedBy
)

fun LayerDto.toLayer(): Layer = Layer(
    layerid = layerid,
    name = name,
    sortId = sortId,
    shown = shown,
    color = color,
    networkConnectionId = networkConnectionId,
    createdAt = createdAt,
    lastchangedAt = lastchangedAt,
    lastchangedBy = lastchangedBy
)