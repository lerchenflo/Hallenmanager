package com.lerchenflo.hallenmanager.data.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.LayerDto
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
        parentColumn = "itemid",
        entityColumn = "layerid",
        associateBy = Junction(ItemLayerCrossRef::class)
    )
    val layers: List<LayerDto>
)