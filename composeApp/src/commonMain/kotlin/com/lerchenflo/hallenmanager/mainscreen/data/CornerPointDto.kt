package com.lerchenflo.hallenmanager.mainscreen.data

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity()
@Serializable
data class CornerPointDto(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val itemId: String,
    val offsetX: Float,
    val offsetY: Float
){
    fun asOffset(): Offset{
        return Offset(offsetX, offsetY)
    }
}