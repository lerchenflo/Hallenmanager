package com.lerchenflo.hallenmanager.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.data.CornerPointDto
import com.lerchenflo.hallenmanager.data.ItemDto
import com.lerchenflo.hallenmanager.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.util.isFuzzySubsequence
import com.lerchenflo.hallenmanager.util.levenshteinWithinThreshold
import com.lerchenflo.hallenmanager.util.normalizeForSearch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class Item(
    val itemid: Long = 0L,
    val areaId: Long,
    val title: String,
    val description: String,
    val layers: List<Layer>,
    val color: Long?,
    val lastchanged: String = Clock.System.now().toEpochMilliseconds().toString(),
    val created : String = Clock.System.now().toEpochMilliseconds().toString(),
    val cornerPoints: List<Offset>
) {

    /**
     * Returns the max priority this item has (Higher value higher priority)
     *
     */
    fun getPriority(): Int {
        return layers.maxOfOrNull { it.sortId } ?: 0
    }

    fun isVisible(): Boolean {
        if (layers.any { it.shown }) return true //If any of this items layers is shown
        if (layers.isEmpty()) return true //If no layer is selected, mark as visible if it has a color
        return false
    }

    /**
     * Returns the absolute color the item will have, including all layers
     *
     */
    fun getAbsoluteColor(): Color {
        val layercolor = layers
            .filter { it.shown }
            .maxByOrNull { it.sortId }
            ?.getColor()

        //Return itemcolor, else layercolor
        return if (color == null){
            layercolor ?: Color.Black
        }else {
            Color(color.toULong())
        }
    }

    /**
     * Returns the custom color which the user may have picked
     */
    fun getCustomColor(): Color? {
        return if (color != null){
            Color(color.toULong())
        } else null
    }

    fun matchesSearchQuery(query: String): Boolean {
        val q = query.trim()
        if (q.isEmpty()) return true // treat empty query as match (change if you want false)

        val titleNorm = title.normalizeForSearch()
        val descNorm = description.normalizeForSearch()
        val combined = (title + " " + description).normalizeForSearch()

        val qNorm = q.normalizeForSearch()

        // direct checks
        if (titleNorm == qNorm || descNorm == qNorm || combined == qNorm) return true
        if (titleNorm.contains(qNorm) || descNorm.contains(qNorm) || combined.contains(qNorm)) return true
        if (titleNorm.startsWith(qNorm) || descNorm.startsWith(qNorm)) return true

        // tokenized: all tokens from query must be matched somewhere (in any order)
        val qTokens = qNorm.split(" ").filter { it.isNotBlank() }
        val titleWords = titleNorm.split(" ").filter { it.isNotBlank() }
        val descWords = descNorm.split(" ").filter { it.isNotBlank() }
        val allWords = (titleWords + descWords).distinct()

        // helper: for a single token, check if any word roughly matches
        fun tokenMatches(token: String): Boolean {
            // exact contains on any word
            if (allWords.any { it.contains(token) }) return true
            // small fuzzy distance (<=1) on any word
            if (allWords.any { levenshteinWithinThreshold(it, token, 1) }) return true
            // token as fuzzy subsequence of combined (characters in order with gaps)
            if (isFuzzySubsequence(token, titleNorm) || isFuzzySubsequence(token, descNorm) || isFuzzySubsequence(token, combined)) return true
            return false
        }

        if (qTokens.all { tokenMatches(it) }) return true

        // acronym matching (e.g. "hm" for "Hallen Manager")
        val acronym = titleWords.mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
        if (acronym.contains(qNorm)) return true

        // fallback: allow up to 1 edit between whole query and title/description
        if (levenshteinWithinThreshold(titleNorm, qNorm, 1)) return true
        if (levenshteinWithinThreshold(descNorm, qNorm, 1)) return true

        return false
    }

    fun getCenter(): Offset{
        var x = 0f
        var y = 0f
        cornerPoints.forEach { point ->
            x += point.x
            y += point.y
        }
        return Offset(x / cornerPoints.size, y / cornerPoints.size)
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
        itemid = itemid,
        title = title,
        areaId = areaid,
        description = description,
        lastChanged = lastchanged,
        created = created,
        color = color
    ),
    cornerPoints = cornerPoints.map {
        CornerPointDto(
            itemId = itemid,
            offsetX = it.x,
            offsetY = it.y
        )
    },
    layers = layers.map {
        it.toLayerDto()
    }
)

fun ItemWithListsDto.toItem(): Item = Item(
    itemid = item.itemid,
    areaId = item.areaId,
    title = item.title,
    description = item.description,
    layers = layers.map {
        it.toLayer()
    },
    lastchanged = item.lastChanged,
    created = item.created,
    color = item.color,
    cornerPoints = cornerPoints.map {
        it.asOffset()
    }
)