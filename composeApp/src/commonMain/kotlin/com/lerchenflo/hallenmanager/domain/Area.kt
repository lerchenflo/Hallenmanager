package com.lerchenflo.hallenmanager.domain

import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.AreaWithItemsDto
import com.lerchenflo.hallenmanager.domain.Item

data class Area(
    var id: Long = 0L,
    var name: String,
    var description: String,
    var items: List<Item>
) {
}


fun Area.toAreaDto(): AreaWithItemsDto = AreaWithItemsDto(
    area = AreaDto(
        name = name,
        description = description
    ),
    items = items.map {
        it.toItemDto(areaid = id)
    }
)

fun AreaWithItemsDto.toArea(): Area = Area(
    id = area.id,
    name = area.name,
    description = area.description,
    items = items.map {
        it.toItem()
    }
)