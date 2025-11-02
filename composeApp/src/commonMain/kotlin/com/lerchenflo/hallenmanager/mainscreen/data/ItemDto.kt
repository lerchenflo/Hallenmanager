package com.lerchenflo.hallenmanager.mainscreen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Entity()
@Serializable
data class ItemDto(
    @PrimaryKey(autoGenerate = false)
    val itemid: String = "",
    val title: String,
    val areaId: String,
    val description: String,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
    val color: Long?,

    //Is this item on its area or is it in the short access menu
    val onArea: Boolean,

)