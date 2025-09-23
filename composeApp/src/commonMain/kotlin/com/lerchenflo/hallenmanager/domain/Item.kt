package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.geometry.Offset
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.ItemWithListsDto
import com.lerchenflo.hallenmanager.data.LayerDto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class Item(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val layer: List<Layer>,
    val lastchanged: String = Clock.System.now().toEpochMilliseconds().toString(),
    val created : String = Clock.System.now().toEpochMilliseconds().toString(),
    val cornerPoints: List<Offset>
) {

    fun getCenter(): Offset{
        var x = 0f
        var y = 0f

        cornerPoints.forEach { point ->
            x += point.x
            y += point.y
        }

        return Offset(x, y)
    }
}

fun Item.toItemDto(areaid: Long): ItemWithListsDto = ItemWithListsDto(
    item = ItemDto(
        id = id,
        title = title,
        areaId = areaid,
        description = description,
        lastChanged = lastchanged,
        created = created
    ),
    cornerPoints = cornerPoints.map {
        CornerPointDto(
            itemId = id,
            offsetX = it.x,
            offsetY = it.y
        )
    },
    layers = layer.map {
        it.toLayerDto()
    }
)

fun ItemWithListsDto.toItem(): Item = Item(
    id = item.id,
    title = item.title,
    description = item.description,
    layer = layers.map {
        it.toLayer()
    },
    lastchanged = item.lastChanged,
    created = item.created,
    cornerPoints = cornerPoints.map {
        it.asOffset()
    }
)