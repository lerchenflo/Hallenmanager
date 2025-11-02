@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.mainscreen.domain

import com.lerchenflo.hallenmanager.mainscreen.data.AreaDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.AreaWithItemsDto
import kotlin.time.ExperimentalTime

data class Area(
    var id: String = "",
    var name: String,
    var description: String,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
    var networkConnectionId: Long?,
    var items: List<Item>
) {
    fun isRemoteArea() : Boolean {
        return networkConnectionId != null
    }
}


fun Area.toAreaWithItemsDto(): AreaWithItemsDto = AreaWithItemsDto(
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