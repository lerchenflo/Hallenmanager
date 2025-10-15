package com.lerchenflo.hallenmanager.mainscreen.data.relations

import androidx.room.Entity
import androidx.room.Index

@Entity(primaryKeys = ["itemid", "layerid"],
    indices = [Index(value = ["itemid"])])
data class ItemLayerCrossRef(
    val itemid: Long,
    val layerid: Long
)