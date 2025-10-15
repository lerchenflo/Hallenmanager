package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DensitySmall
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.util.snapToGrid
import com.lerchenflo.hallenmanager.mainscreen.presentation.search.SearchItemUI
import com.lerchenflo.hallenmanager.sharedUi.LegendOverlay
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_area
import hallenmanager.composeapp.generated.resources.custom_paint
import hallenmanager.composeapp.generated.resources.custom_paint_painting
import hallenmanager.composeapp.generated.resources.hide_shortaccess_menu
import hallenmanager.composeapp.generated.resources.layers
import hallenmanager.composeapp.generated.resources.searchbarhint
import hallenmanager.composeapp.generated.resources.show_shortaccess_menu
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun MainScreenRoot(
    viewmodel: MainScreenViewmodel,
){
    val loadfinished = viewmodel.initialLoadFinished.collectAsStateWithLifecycle()

    if (loadfinished.value){
        MainScreen(
            state = viewmodel.state,
            onAction = viewmodel::onAction
        )
    } else {
        Box(
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator()
        }
    }



}



@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun MainScreen(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {
    val maxzoomlevel = 8f
    val minzoomlevel = 0.02f


    var localScale by remember { mutableStateOf(1f) }
    var localOffset by remember { mutableStateOf(Offset.Zero) }

    //Visible area size
    var viewportSize by remember { mutableStateOf(IntSize(0, 0)) }

    var searchbaractive by remember { mutableStateOf(false) }

    var fabmenuexpanded by remember { mutableStateOf(false) }


    var draggedItem by remember { mutableStateOf<Item?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var itemScreenPosition by remember { mutableStateOf(Offset.Zero) }



    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            modifier = Modifier.weight(1f),
            floatingActionButton = {
                if (state.currentArea != null){
                    //Floatingactionbutton for settings
                    FloatingActionButtonMenu(
                        expanded = fabmenuexpanded,
                        button = {
                            ToggleFloatingActionButton(
                                checked = fabmenuexpanded,
                                onCheckedChange = {
                                    fabmenuexpanded = !fabmenuexpanded
                                },
                                content = {
                                    Icon(
                                        imageVector = Icons.Outlined.DensitySmall,
                                        contentDescription = "Show context menu",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            )

                        },
                        modifier = Modifier,
                        content = {

                            FloatingActionButtonMenuItem(
                                onClick = {onAction(MainScreenAction.OnLayersClicked)},
                                text = {
                                    Text(
                                        text = stringResource(Res.string.layers)
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Layers,
                                        contentDescription = "Navigate to Layers",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            )

                            FloatingActionButtonMenuItem(
                                onClick = {
                                    if (state.isDrawing){
                                        onAction(MainScreenAction.OnStopPainting)
                                    }else {
                                        onAction(MainScreenAction.OnStartPainting)
                                    }
                                },
                                text = {
                                    Text(
                                        text = if (state.isDrawing) stringResource(Res.string.custom_paint_painting) else stringResource(Res.string.custom_paint)
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (state.isDrawing) Icons.Rounded.Close else Icons.Outlined.Draw,
                                        contentDescription = "Draw custom",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            )


                            FloatingActionButtonMenuItem(
                                onClick = {
                                    onAction(MainScreenAction.OnShowShortAccessMenuClick(!state.showShortAccessMenu))
                                },
                                text = {
                                    Text(
                                        text = if (state.showShortAccessMenu) stringResource(Res.string.hide_shortaccess_menu) else stringResource(Res.string.show_shortaccess_menu)
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (state.showShortAccessMenu) Icons.Rounded.ArrowDropDown else Icons.Rounded.ArrowDropUp,
                                        contentDescription = "Short access menu toggle",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            )


                        }
                    )
                }
            },
            content = {scaffoldPadding ->
                if (state.iteminfopopupshown) {
                    CreateItemPopup(
                        onAction = onAction,
                        state = state
                    )
                }

                if (state.areainfopopupshown) {
                    CreateAreaPopup(
                        onAction = onAction,
                        state = state
                    )
                }




                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ){


                    if (state.currentArea != null){ //Allow painting only if an area is selected


                        //Titlerow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            //Seach textfield

                            val focusManager = LocalFocusManager.current

                            SearchBar(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(
                                        start = 8.dp,
                                        bottom = 8.dp,
                                        end = 8.dp
                                    ),
                                inputField = {
                                    InputField(
                                        placeholder = { Text(stringResource(Res.string.searchbarhint)) },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search"
                                            )
                                        },
                                        trailingIcon = {
                                            if (state.searchterm.isNotEmpty()){
                                                IconButton(
                                                    onClick = {
                                                        onAction(MainScreenAction.OnSearchtermChange(""))
                                                        searchbaractive = false
                                                    }
                                                ){
                                                    Icon(
                                                        imageVector = Icons.Rounded.Close,
                                                        contentDescription = "Clear searchquery"
                                                    )
                                                }
                                            }
                                        },
                                        query = state.searchterm,
                                        // open the search suggestions as soon as user types (or clicks)
                                        onQueryChange = {
                                            searchbaractive = true                 // <- IMPORTANT: open on click/typing
                                            onAction(MainScreenAction.OnSearchtermChange(it))
                                        },
                                        onSearch = {
                                            searchbaractive = false                // hide when user confirms search
                                            focusManager.clearFocus()
                                        },
                                        expanded = searchbaractive,
                                        // make InputField report expand/collapse events back to our boolean
                                        onExpandedChange = { searchbaractive = it }
                                    )
                                },
                                // SearchBar's expanded/onExpandedChange must also be in sync
                                expanded = searchbaractive,
                                onExpandedChange = { searchbaractive = it },
                                content = {
                                    if (state.searchterm.isNotEmpty()){
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                        ) {
                                            items(items = state.currentSearchResult) { searchitem ->
                                                SearchItemUI(
                                                    searchItem = searchitem,
                                                    onClick = {
                                                        //Switch do different area if item is not in current area
                                                        if (state.currentArea.id != searchitem.item.areaId) {
                                                            onAction(
                                                                MainScreenAction.OnSelectArea(
                                                                    searchitem.item.areaId
                                                                )
                                                            )
                                                        }


                                                        searchbaractive = false
                                                        focusManager.clearFocus()
                                                        onAction(
                                                            MainScreenAction.OnSearchtermChange(
                                                                ""
                                                            )
                                                        )


                                                        val targetScale = 1f
                                                        localScale = targetScale

                                                        val viewportCenterX =
                                                            viewportSize.width / 2f
                                                        val viewportCenterY =
                                                            viewportSize.height / 2f
                                                        val itemCenter = searchitem.item.getCenter()

                                                        //println("Height: $viewportCenterY Width: $viewportCenterX")
                                                        localOffset = Offset(
                                                            x = viewportCenterX - itemCenter.x * localScale,
                                                            y = viewportCenterY - itemCenter.y * localScale
                                                        )


                                                    }
                                                )

                                            }
                                        }
                                    }


                                }
                            )

                            var areadropdownexpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .padding(
                                        top = scaffoldPadding.calculateTopPadding(),
                                        end = 8.dp
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                TextButton(
                                    onClick = { areadropdownexpanded = true },
                                ){
                                    Text(
                                        text = state.currentArea.name
                                    )
                                }


                                DropdownMenu(
                                    expanded = areadropdownexpanded,
                                    onDismissRequest = { areadropdownexpanded = false },
                                ){
                                    state.availableAreas.forEach { availablearea ->

                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CropPortrait,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )

                                                    Spacer(modifier = Modifier.width(8.dp))

                                                    Column {
                                                        Text(
                                                            text = availablearea.name,
                                                            maxLines = 1
                                                        )

                                                        Text(
                                                            text = availablearea.description,
                                                            maxLines = 1,
                                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                onAction(MainScreenAction.OnSelectArea(availablearea.id))
                                                areadropdownexpanded = false
                                            }
                                        )
                                    }

                                    //Add button
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                Text(
                                                    text = stringResource(Res.string.add_area)
                                                )
                                            }
                                        },
                                        onClick = {
                                            areadropdownexpanded = false
                                            onAction(MainScreenAction.OnCreateAreaStart)
                                        }
                                    )

                                }
                            }
                        }


                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .onSizeChanged {
                                    //only update if not searching, else the centering wont work because the height is 0
                                    if (!searchbaractive) viewportSize = it
                                }


                        ){



                            //Canvasbox
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                                    .clipToBounds()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { centroid, pan, zoom, rotation ->
                                            val oldScale = localScale
                                            val newScale = (oldScale * zoom).coerceIn(minzoomlevel, maxzoomlevel)
                                            val scaleChange = if (oldScale == 0f) 1f else newScale / oldScale

                                            // Keep the content point under the centroid fixed while zooming, and add pan
                                            localOffset = Offset(
                                                x = centroid.x - scaleChange * (centroid.x - localOffset.x) + pan.x,
                                                y = centroid.y - scaleChange * (centroid.y - localOffset.y) + pan.y
                                            )

                                            localScale = newScale

                                        }
                                    }

                                    .pointerInput(state.currentArea.items) {
                                        detectTapGestures(
                                            onTap = { raw ->
                                                val contentPoint = (raw - localOffset) / localScale
                                                onAction(MainScreenAction.OnClick(contentPoint, false))
                                            },
                                            onLongPress = { raw ->

                                                val contentPoint = (raw - localOffset) / localScale
                                                onAction(MainScreenAction.OnClick(contentPoint, true))

                                            },
                                            onDoubleTap = { raw ->

                                                /*
                                                // Double-tap: zoom in/out focusing at tap point
                                                val target = if (localScale < maxzoomlevel) (localScale * 2f).coerceAtMost(maxzoomlevel) else minzoomlevel
                                                val scaleChange = target / localScale
                                                localOffset = Offset(
                                                    x = raw.x - scaleChange * (raw.x - localOffset.x),
                                                    y = raw.y - scaleChange * (raw.y - localOffset.y)
                                                )
                                                localScale = target

                                                 */
                                            },
                                        )
                                    }
                                    .graphicsLayer {
                                        transformOrigin = TransformOrigin(0f, 0f)
                                        translationX = localOffset.x
                                        translationY = localOffset.y
                                        scaleX = localScale
                                        scaleY = localScale
                                    }

                            ){


                                LaunchedEffect(localScale, localOffset, viewportSize) {
                                    snapshotFlow { Triple(localScale, localOffset, viewportSize) }
                                        .debounce(2)
                                        .collect { (scale, offset, size) ->
                                            onAction(MainScreenAction.OnZoom(scale, offset, size))
                                        }
                                }

                                val gridLines = remember(localScale, localOffset, viewportSize, state.gridspacing) {
                                    data class GridLines(
                                        val xs: FloatArray,
                                        val ys: FloatArray,
                                        val visibleLeft: Float,
                                        val visibleTop: Float,
                                        val visibleRight: Float,
                                        val visibleBottom: Float
                                    ) {
                                        override fun equals(other: Any?): Boolean {
                                            if (this === other) return true
                                            if (other == null || this::class != other::class) return false

                                            other as GridLines

                                            if (visibleLeft != other.visibleLeft) return false
                                            if (visibleTop != other.visibleTop) return false
                                            if (visibleRight != other.visibleRight) return false
                                            if (visibleBottom != other.visibleBottom) return false
                                            if (!xs.contentEquals(other.xs)) return false
                                            if (!ys.contentEquals(other.ys)) return false

                                            return true
                                        }

                                        override fun hashCode(): Int {
                                            var result = visibleLeft.hashCode()
                                            result = 31 * result + visibleTop.hashCode()
                                            result = 31 * result + visibleRight.hashCode()
                                            result = 31 * result + visibleBottom.hashCode()
                                            result = 31 * result + xs.contentHashCode()
                                            result = 31 * result + ys.contentHashCode()
                                            return result
                                        }
                                    }

                                    // compute visible bounds in content-space
                                    val visibleLeft = (-localOffset.x / localScale).coerceAtLeast(0f)
                                    val visibleTop = (-localOffset.y / localScale).coerceAtLeast(0f)
                                    val visibleRight = (visibleLeft + (viewportSize.width / localScale))
                                    val visibleBottom = (visibleTop + (viewportSize.height / localScale))

                                    // compute start indices and counts (integers) to avoid repeated float math
                                    val startXIndex = floor(visibleLeft / state.gridspacing).toInt().coerceAtLeast(0)
                                    val endXIndex = ceil(visibleRight / state.gridspacing).toInt()
                                    val startYIndex = floor(visibleTop / state.gridspacing).toInt().coerceAtLeast(0)
                                    val endYIndex = ceil(visibleBottom / state.gridspacing).toInt()

                                    // create FloatArrays of positions (reused as immutable data)
                                    val xs = FloatArray((endXIndex - startXIndex + 1).coerceAtLeast(0)) { i ->
                                        (startXIndex + i) * state.gridspacing
                                    }
                                    val ys = FloatArray((endYIndex - startYIndex + 1).coerceAtLeast(0)) { i ->
                                        (startYIndex + i) * state.gridspacing
                                    }

                                    GridLines(xs, ys, visibleLeft, visibleTop, visibleRight, visibleBottom)
                                }

                                Canvas(
                                    modifier = Modifier
                                        .size(10000.dp)
                                ) {
                                    val (xs, ys, visibleLeft, visibleTop, visibleRight, visibleBottom) = gridLines

                                    for (i in xs.indices) {
                                        val x = xs[i]
                                        drawLine(
                                            color = Color.Gray,
                                            start = Offset(x, visibleTop),
                                            end = Offset(x, visibleBottom),
                                            strokeWidth = 1f / localScale
                                        )
                                    }

                                    for (i in ys.indices) {
                                        val y = ys[i]
                                        drawLine(
                                            color = Color.Gray,
                                            start = Offset(visibleLeft, y),
                                            end = Offset(visibleRight, y),
                                            strokeWidth = 1f / localScale
                                        )
                                    }


                                    //Current drawing lines
                                    val points = state.currentDrawingOffsets
                                    if (points.size >= 2) {
                                        for (i in 0 until points.lastIndex) {
                                            drawLine(
                                                color = Color.Black,
                                                start = points[i],
                                                end = points[i + 1],
                                                strokeWidth = 20f / localScale,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                    //Current drawing corner points
                                    drawPoints(
                                        points,
                                        color = Color.Red,
                                        strokeWidth = 30f / localScale,
                                        pointMode = PointMode.Points

                                    )



                                }


                                //Draw items in this area
                                state.currentArea.items
                                    .forEach { item ->
                                        if (item.cornerPoints.size > 2) {
                                            key("${item.itemid}_${item.title}") {
                                                ItemPolygon(
                                                    item = item,
                                                    scale = localScale,
                                                )
                                            }
                                        }
                                    }


                                //If placed here preview is in correct size but moving is corrupted
                            }

                            //if placed here the preview is not in correct size while zooming but follows the pointer correctly
                            if (draggedItem != null) {
                                // Convert screen position to content position for preview
                                val contentOffset = (dragPosition - localOffset) / localScale

                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            transformOrigin = TransformOrigin(0f, 0f)
                                            translationX = localOffset.x
                                            translationY = localOffset.y
                                            scaleX = localScale
                                            scaleY = localScale
                                        }
                                ) {
                                    ItemPolygon(
                                        item = draggedItem!!,
                                        scale = localScale,
                                        offset = contentOffset
                                    )
                                }
                            }


                            LegendOverlay(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(6.dp),
                                scale = localScale,
                                gridSpacingInContentPx = state.gridspacing,
                                metersPerGrid = state.gridspacing / 50
                            )
                        }





                    }else{
                        //If no area is selected show add popup
                        CreateFirstAreaPopup(
                            onclick = {
                                onAction(MainScreenAction.OnCreateAreaStart)
                            }
                        )
                    }


                }
            }
        )




        // Replace the entire LazyRow section with this improved version:

        if (state.showShortAccessMenu && state.currentArea != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                val thumbnailDp = 50.dp
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = state.shortAccessItems) { item ->
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .wrapContentHeight()
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.localToWindow(Offset.Zero)
                                    itemScreenPosition = position
                                }
                                .pointerInput(item.itemid) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { startOffset ->
                                            draggedItem = item
                                            dragPosition = itemScreenPosition + startOffset
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragPosition += dragAmount
                                        },
                                        onDragEnd = {
                                            val contentPoint = (dragPosition - localOffset) / localScale
                                            val snapped = snapToGrid(contentPoint, state.gridspacing)

                                            onAction(
                                                MainScreenAction.OnMoveItemToGrid(
                                                    item = item,
                                                    position = snapped
                                                )
                                            )

                                            draggedItem = null
                                            dragPosition = Offset.Zero
                                        },
                                        onDragCancel = {
                                            draggedItem = null
                                            dragPosition = Offset.Zero
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.size(thumbnailDp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ItemPolygon(
                                        item = item,
                                        scale = 1f,
                                        targetSize = DpSize(thumbnailDp, thumbnailDp),
                                        showTitle = false
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }


}


@Preview(
    showBackground = true,
    name = "Mainscreen",
)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        state = MainScreenState(),
        onAction = {}
    )
}