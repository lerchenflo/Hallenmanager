@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.mainscreen.domain

import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.AreaWithItemsDto
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Area(
    var id: String = "",
    var name: String,
    var description: String,
    var createdAt: Instant,
    var lastchangedAt: Instant,
    var lastchangedBy: String,
    var networkConnectionId: Long?,
    var items: List<Item>
) {
    fun isRemoteArea() : Boolean {
        return networkConnectionId != null
    }
}


fun Area.toAreaDto(): AreaWithItemsDto = AreaWithItemsDto(
    area = AreaDto(
        name = name,
        description = description,
        createdAt = createdAt,
        id = id,
        lastchangedAt = lastchangedAt,
        lastchangedBy = lastchangedBy,
        networkConnectionId = networkConnectionId,
    ),
    items = items.map {
        it.toItemDto(areaid = id)
    }
)

fun Area.toAreaWithoutItemsDto(): AreaDto = AreaDto(
    name = name,
    description = description,
    createdAt = createdAt,
    id = id,
    lastchangedAt = lastchangedAt,
    lastchangedBy = lastchangedBy,
    networkConnectionId = networkConnectionId,
)


fun AreaDto.toArea() : Area = Area(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    lastchangedAt = lastchangedAt,
    lastchangedBy = lastchangedBy,
    networkConnectionId = networkConnectionId,
    items = emptyList()
)

fun AreaWithItemsDto.toArea(): Area = Area(
    id = area.id,
    name = area.name,
    description = area.description,
    createdAt = area.createdAt,
    lastchangedAt = area.lastchangedAt,
    lastchangedBy = area.lastchangedBy,
    networkConnectionId = area.networkConnectionId,
    items = items.map {
        it.toItem()
    }
)