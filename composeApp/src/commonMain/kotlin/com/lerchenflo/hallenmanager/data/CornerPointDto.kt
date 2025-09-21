package com.lerchenflo.hallenmanager.data

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity()
@Serializable
data class CornerPointDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val itemId: Long,
    val offsetX: Float,
    val offsetY: Float
){
    fun asOffset(): Offset{
        return Offset(offsetX, offsetY)
    }
}