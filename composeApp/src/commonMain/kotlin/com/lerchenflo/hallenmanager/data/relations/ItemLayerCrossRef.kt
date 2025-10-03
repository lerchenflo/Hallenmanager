package com.lerchenflo.hallenmanager.data.relations

import androidx.room.Entity

@Entity(primaryKeys = ["itemid", "layerid"])
data class ItemLayerCrossRef(
    val itemid: Long,
    val layerid: Long
)
