package com.lerchenflo.hallenmanager.mainscreen.domain

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.lerchenflo.hallenmanager.mainscreen.data.CornerPointDto
import com.lerchenflo.hallenmanager.mainscreen.data.ItemDto
import com.lerchenflo.hallenmanager.mainscreen.data.relations.ItemWithListsDto
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayer
import com.lerchenflo.hallenmanager.layerselection.domain.toLayerDto
import com.lerchenflo.hallenmanager.util.isFuzzySubsequence
import com.lerchenflo.hallenmanager.util.levenshteinWithinThreshold
import com.lerchenflo.hallenmanager.util.normalizeForSearch
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Item(
    val itemid: String = "",
    val areaId: String,
    val title: String,
    val description: String,
    val layers: List<Layer>,
    val color: Long?,
    val onArea: Boolean,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
    val cornerPoints: List<CornerPointDto>,
    var networkConnectionId: Long?
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
            layercolor ?: Color.Yellow
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
            x += point.offsetX
            y += point.offsetY
        }
        return Offset(x / cornerPoints.size, y / cornerPoints.size)
    }

    fun isRemoteItem() : Boolean {
        return networkConnectionId != null
    }
}

/**
 * Return a new Item with cornerPoints translated so their bounding box's
 * top-left (minX,minY) is at Offset(0f, 0f).
 *
 * This makes the polygon "touch" the X and Y axes (as near as possible)
 * by shifting all points by (-minX, -minY).
 */
fun Item.withCornerPointsAtOrigin(): Item {
    if (cornerPoints.isEmpty()) return this

    val minX = cornerPoints.minOfOrNull { it.offsetX } ?: 0f
    val minY = cornerPoints.minOfOrNull { it.offsetY } ?: 0f

    // If already at origin, return unchanged
    if (minX == 0f && minY == 0f) return this

    cornerPoints.forEach { point ->
        point.offsetX -= minX
        point.offsetY -= minY
    }

    return this.copy(cornerPoints = cornerPoints)
}


fun Item.toItemDto(): ItemWithListsDto = ItemWithListsDto(
    item = ItemDto(
        itemid = itemid,
        title = title,
        areaId = areaId,
        description = description,
        lastchangedAt = lastchangedAt,
        createdAt = createdAt,
        color = color,
        onArea = onArea,
        lastchangedBy = lastchangedBy,
        networkConnectionId = networkConnectionId
    ),
    cornerPoints = cornerPoints.map {
        CornerPointDto(
            id = it.id,
            itemId = itemid,
            offsetX = it.offsetX,
            offsetY = it.offsetY,
            networkConnectionId = networkConnectionId
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
    lastchangedAt = item.lastchangedAt,
    createdAt = item.createdAt,
    color = item.color,
    onArea = item.onArea,
    cornerPoints = cornerPoints,
    lastchangedBy = item.lastchangedBy,
    networkConnectionId = item.networkConnectionId
)