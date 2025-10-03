package com.lerchenflo.hallenmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity()
@Serializable
data class ItemDto(
    @PrimaryKey(autoGenerate = true)
    val itemid: Long = 0L,
    val title: String,
    val areaId: Long,
    val description: String,
    val lastChanged: String,
    val created: String,
    val color: Long?
)