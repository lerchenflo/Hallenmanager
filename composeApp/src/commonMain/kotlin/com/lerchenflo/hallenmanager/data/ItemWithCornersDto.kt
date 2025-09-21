package com.lerchenflo.hallenmanager.data

import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class ItemWithCornersDto(
    @Embedded val item: ItemDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "itemId",
        entity = CornerPointDto::class
    )
    val cornerPoints: List<CornerPointDto>
)