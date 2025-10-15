@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.domain

import com.lerchenflo.hallenmanager.data.AreaDto
import com.lerchenflo.hallenmanager.data.relations.AreaWithItemsDto
import com.lerchenflo.hallenmanager.domain.Item
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Area(
    var id: Long = 0L,
    var name: String,
    var description: String,
    var createdAt: Instant,
    var lastchangedAt: Instant,
    var lastchangedBy: String,
    var items: List<Item>
) {
}


fun Area.toAreaDto(): AreaWithItemsDto = AreaWithItemsDto(
    area = AreaDto(
        name = name,
        description = description,
        createdAt = createdAt,
        id = id,
        lastchangedAt = lastchangedAt,
        lastchangedBy = lastchangedBy
    ),
    items = items.map {
        it.toItemDto(areaid = id)
    }
)

fun AreaWithItemsDto.toArea(): Area = Area(
    id = area.id,
    name = area.name,
    description = area.description,
    createdAt = area.createdAt,
    lastchangedAt = area.lastchangedAt,
    lastchangedBy = area.lastchangedBy,
    items = items.map {
        it.toItem()
    }
)