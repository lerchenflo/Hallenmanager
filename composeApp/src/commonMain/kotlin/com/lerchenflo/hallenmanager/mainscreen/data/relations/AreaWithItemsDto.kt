package com.lerchenflo.hallenmanager.mainscreen.data.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
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