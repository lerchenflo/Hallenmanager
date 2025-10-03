package com.lerchenflo.hallenmanager.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.relations.ItemWithListsDto
import kotlinx.serialization.Serializable

@Serializable
data class AreaWithItemsDto(
    @Embedded val area: AreaDto,
    @Relation(
        parentColumn = "id",
        entityColumn = "areaId",
        entity = ItemDto::class
    )
    val items: List<ItemWithListsDto> // nested relation: Room supports nested @Relation POJOs
)