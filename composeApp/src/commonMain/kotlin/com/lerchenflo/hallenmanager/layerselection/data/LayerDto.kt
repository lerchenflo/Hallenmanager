package com.lerchenflo.hallenmanager.layerselection.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class LayerDto(
    @PrimaryKey(autoGenerate = false)
    val layerid: String = "",
    val name: String,
    val sortId: Int,
    val shown: Boolean,
    val color: Long,
    var networkConnectionId: Long?,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
)