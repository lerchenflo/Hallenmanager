package com.lerchenflo.hallenmanager.mainscreen.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.lerchenflo.hallenmanager.layerselection.data.LayerDto
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import kotlinx.serialization.Serializable

@Serializable
data class ItemWithListsDto(
    @Embedded val item: ItemDto,
    @Relation(
        parentColumn = "itemid",
        entityColumn = "itemId"
    )
    val cornerPoints: List<CornerPointDto>,

    @Relation(
    //TODO fix this to resolve from list of strings
        parentColumn = "itemid",
        entityColumn = "layerid",
    )
    val layers: List<LayerDto>
)