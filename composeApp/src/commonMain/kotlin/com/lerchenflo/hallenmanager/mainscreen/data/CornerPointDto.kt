package com.lerchenflo.hallenmanager.mainscreen.data

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Entity()
@Serializable
data class CornerPointDto(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val itemId: String,
    var offsetX: Float,
    var offsetY: Float,

    var networkConnectionId: Long?
){
    fun asOffset(): Offset{
        return Offset(offsetX, offsetY)
    }

    fun isRemote() : Boolean {
        return networkConnectionId != null
    }

    fun setOffset(offset: Offset) : CornerPointDto {
        offsetX = offset.x
        offsetY = offset.y
        return this
    }
}