package com.lerchenflo.hallenmanager.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class AreaWithItemsDto(
    @Embedded val area: AreaDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "areaId",
        entity = ItemDto::class
    )
    val items: List<ItemWithCornersDto> // nested relation: Room supports nested @Relation POJOs
)