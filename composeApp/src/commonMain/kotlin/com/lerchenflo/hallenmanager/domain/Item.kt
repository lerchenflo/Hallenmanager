package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
    val color: Long?,
    val lastchanged: String = Clock.System.now().toEpochMilliseconds().toString(),
    val created : String = Clock.System.now().toEpochMilliseconds().toString(),
    val cornerPoints: List<Offset>
) {


    fun getColor(): Color {

        println("getcolor long: $color")

        val layercolor = layer.maxByOrNull { layer ->
            layer.sortId
        }?.getColor()

        return if (color == null){
            layercolor ?: Color.Black
        }else {
            println("Returnedcolor: ${Color(color)}")
            Color(color.toULong())

        }
    }

    fun getCenter(): Offset{
        var x = 0f
        var y = 0f
        cornerPoints.forEach { point ->
            x += point.x
            y += point.y
        }
        return Offset(x, y)
    }

    fun isPolygonClicked(pt: Offset): Boolean {
        if (cornerPoints.size < 3) return false

        // quick reject via bounding box
        val minX = cornerPoints.minOf { it.x }
        val maxX = cornerPoints.maxOf { it.x }
        val minY = cornerPoints.minOf { it.y }
        val maxY = cornerPoints.maxOf { it.y }
        if (pt.x < minX || pt.x > maxX || pt.y < minY || pt.y > maxY) return false

        // ray casting algorithm
        var inside = false
        var j = cornerPoints.lastIndex
        for (i in 0..cornerPoints.lastIndex) {
            val xi = cornerPoints[i].x; val yi = cornerPoints[i].y
            val xj = cornerPoints[j].x; val yj = cornerPoints[j].y

            // check if the ray intersects edge (i,j)
            val intersects = ((yi > pt.y) != (yj > pt.y)) &&
                    (pt.x < (xj - xi) * (pt.y - yi) / (yj - yi) + xi)
            if (intersects) inside = !inside
            j = i
        }
        return inside
    }
}

fun Item.toItemDto(areaid: Long): ItemWithListsDto = ItemWithListsDto(
    item = ItemDto(
        id = id,
        title = title,
        areaId = areaid,
        description = description,
        lastChanged = lastchanged,
        created = created,
        color = color
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
    color = item.color,
    cornerPoints = cornerPoints.map {
        it.asOffset()
    }
)