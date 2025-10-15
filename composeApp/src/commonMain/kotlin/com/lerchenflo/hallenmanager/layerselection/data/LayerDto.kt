package com.lerchenflo.hallenmanager.layerselection.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class LayerDto(
    @PrimaryKey(autoGenerate = true)
    val layerid: Long = 0L,
    val name: String,
    val sortId: Int,
    val shown: Boolean,
    val color: Long
)