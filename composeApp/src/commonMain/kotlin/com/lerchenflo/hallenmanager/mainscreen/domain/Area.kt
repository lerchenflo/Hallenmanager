@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.mainscreen.domain

import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.AreaWithItemsDto
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Area(
    var id: Long = 0L,
    var name: String,
    var description: String,
    var createdAt: Instant,
    var lastchangedAt: Instant,
    var lastchangedBy: String,
    var serverId: Long?,
    var items: List<Item>
) {
    fun isRemoteArea() : Boolean {
        return serverId != null
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
        serverId = serverId
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
    serverId = area.serverId,
    items = items.map {
        it.toItem()
    }
)