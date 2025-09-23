package com.lerchenflo.hallenmanager.data

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class ItemWithListsDto(
    @Embedded val item: ItemDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId",
        entity = CornerPointDto::class
    )
    val cornerPoints: List<CornerPointDto>,


    @Relation(
    parentColumn = "id",
    entityColumn = "layerid",
    entity = LayerDto::class
    )
    val layers: List<LayerDto>
)