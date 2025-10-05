package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.snapToGrid
import com.lerchenflo.hallenmanager.presentation.LegendOverlay
import com.lerchenflo.hallenmanager.presentation.homescreen.search.SearchItemUI
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_area
import hallenmanager.composeapp.generated.resources.searchbarhint
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

    MainScreen(
        state = viewmodel.state,
        onAction = viewmodel::onAction
    )
}



@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
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


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(8.dp),
        floatingActionButton = {
            Column {

                //Floatingactionbutton for stop painting
                if (state.isDrawing){
                    FloatingActionButton(
                        onClick = {
                            onAction(MainScreenAction.OnStopPainting)
                        },
                    ){
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Stop painting",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }


                //Floatingactionbutton for settings
                FloatingActionButton(
                    onClick = {
                        onAction(MainScreenAction.OnSettingsClicked)
                    },
                ){
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Navigate to settings",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        },
        content = {
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
                    .fillMaxSize()
            ){


                if (state.currentArea != null){ //Allow painting only if an area is selected


                    //Titlerow
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = if (!searchbaractive) Alignment.CenterVertically else Alignment.Top
                    ) {

                        //Seach textfield

                        val focusManager = LocalFocusManager.current

                        SearchBar(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
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
                                                        onAction(MainScreenAction.OnSelectArea(searchitem.item.areaId))
                                                    }


                                                    searchbaractive = false
                                                    focusManager.clearFocus()
                                                    onAction(MainScreenAction.OnSearchtermChange(""))


                                                    val targetScale = 1f
                                                    localScale = targetScale

                                                    val viewportCenterX = viewportSize.width / 2f
                                                    val viewportCenterY = viewportSize.height / 2f
                                                    val itemCenter = searchitem.item.getCenter()

                                                    println("Height: $viewportCenterY Width: $viewportCenterX")
                                                    localOffset = Offset(
                                                        x = viewportCenterX - itemCenter.x * localScale,
                                                        y = viewportCenterY - itemCenter.y * localScale
                                                    )


                                                }
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }


                            }
                        )





                        var areadropdownexpanded by remember { mutableStateOf(false) }
                        Box{
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
                                            for (item in state.currentArea.items) { //In viewmodel coroutinescope??

                                                if (isPointInPolygon(contentPoint, item.cornerPoints)) {
                                                    onAction(MainScreenAction.OnItemClicked(item))
                                                    break
                                                }
                                            }
                                        },
                                        onLongPress = { raw ->

                                            val contentPoint = (raw - localOffset) / localScale

                                            val snapped = snapToGrid(contentPoint, state.gridspacing)

                                            onAction(MainScreenAction.OnAddPoint(snapped))
                                        },
                                        onDoubleTap = { raw ->
                                            // Double-tap: zoom in/out focusing at tap point
                                            val target = if (localScale < maxzoomlevel) (localScale * 2f).coerceAtMost(maxzoomlevel) else minzoomlevel
                                            val scaleChange = target / localScale
                                            localOffset = Offset(
                                                x = raw.x - scaleChange * (raw.x - localOffset.x),
                                                y = raw.y - scaleChange * (raw.y - localOffset.y)
                                            )
                                            localScale = target
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
                                )

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
                                            strokeWidth = 20f,
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }
                                //Current drawing corner points
                                drawPoints(
                                    points,
                                    color = Color.Red,
                                    strokeWidth = 30f,
                                    pointMode = PointMode.Points

                                )



                            }


                            //Draw items in this area
                            state.currentArea.items.forEach { item ->
                                if (item.cornerPoints.size > 2) {
                                    key("${item.itemid}_${item.title}") {
                                        ItemPolygon(
                                            item = item,
                                            scale = localScale,
                                            offset = localOffset,

                                        )
                                    }
                                }
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
}


@Preview(
    showBackground = true,
    name = "Mainscreen"
)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        state = MainScreenState(),
        onAction = {}
    )
}