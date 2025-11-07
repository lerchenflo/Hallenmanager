package com.lerchenflo.hallenmanager.layerselection.data

import androidx.room.Entity
import androidx.room.Index

@Entity(primaryKeys = ["itemid", "layerid"],
    indices = [Index(value = ["itemid"]), Index(value = ["layerid"])])
data class ItemLayerCrossRef(val itemid: String, val layerid: String)