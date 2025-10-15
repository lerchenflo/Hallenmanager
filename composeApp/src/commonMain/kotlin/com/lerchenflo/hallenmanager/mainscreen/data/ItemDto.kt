package com.lerchenflo.hallenmanager.mainscreen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Entity()
@Serializable
data class ItemDto(
    @PrimaryKey(autoGenerate = true)
    val itemid: Long = 0L,
    val title: String,
    val areaId: Long,
    val description: String,
    var createdAt: Instant,
    var lastchangedAt: Instant,
    var lastchangedBy: String,
    val color: Long?,

    //Is this item on its area or is it in the short access menu
    val onArea: Boolean,

)