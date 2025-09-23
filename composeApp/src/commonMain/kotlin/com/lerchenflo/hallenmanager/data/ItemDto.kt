package com.lerchenflo.hallenmanager.data

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Entity()
@Serializable
data class ItemDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val areaId: Long,
    val description: String,
    val lastChanged: String,
    val created: String
)