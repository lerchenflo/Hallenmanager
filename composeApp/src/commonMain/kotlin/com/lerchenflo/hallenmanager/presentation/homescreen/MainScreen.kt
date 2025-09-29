package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults.InputField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lerchenflo.hallenmanager.core.di.sharedmodule
import com.lerchenflo.hallenmanager.domain.snapToGrid
import com.lerchenflo.hallenmanager.presentation.LegendOverlay
import com.lerchenflo.hallenmanager.presentation.homescreen.search.SearchItemUI
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_area
import hallenmanager.composeapp.generated.resources.searchbarhint
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.min

@Composable
fun MainScreenRoot(
    viewmodel: MainScreenViewmodel,
){

    MainScreen(
        state = viewmodel.state,
        onAction = viewmodel::onAction
    )
}



@OptIn(ExperimentalMaterial3Api::class)
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
                                // limit the dropdown height so it doesn't occupy the entire screen
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

                        ){


                            val textMeasurer = rememberTextMeasurer()

                            Canvas(
                                modifier = Modifier
                                    .size(10000.dp)

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

                                            onAction(MainScreenAction.OnZoom(newScale))
                                        }
                                    }

                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = { raw ->
                                                val contentPoint = (raw - localOffset) / localScale

                                                val snapped = snapToGrid(contentPoint, state.gridspacing)

                                                state.currentArea.items.forEach { item ->
                                                    if (item.isPolygonClicked(contentPoint)){
                                                        onAction(MainScreenAction.OnItemClicked(item))
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


                            ) {

                                drawRect(Color.LightGray)

                                //Grid
                                var x = 0f
                                while (x <= size.width) {
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 1f / localScale
                                    )
                                    x += state.gridspacing
                                }
                                var y = 0f
                                while (y <= size.height) {
                                    drawLine(
                                        color = Color.Gray,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = 1f / localScale
                                    )
                                    y += state.gridspacing
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


                                //TODO: Replace with composable on top of canvas
                                //Draw items in this area
                                state.currentArea.items.forEach { item ->

                                    if (item.cornerPoints.size > 2){
                                        val path = Path().apply {
                                            fillType = PathFillType.NonZero
                                            moveTo(item.cornerPoints[0].x, item.cornerPoints[0].y)
                                            for (i in 1 until item.cornerPoints.size) {
                                                lineTo(item.cornerPoints[i].x, item.cornerPoints[i].y)
                                            }
                                            close() // close the polygon
                                        }

                                        //Outline color
                                        drawPath(
                                            path = path,
                                            color = item.getColor(),
                                            style = Stroke(width = 4f, cap = StrokeCap.Round)
                                        )

                                        //Fill polygon with color
                                        drawPath(
                                            path = path,
                                            color = item.getColor().copy(alpha = 0.1f),
                                            style = Fill
                                        )


                                        val titleFontSp = (25f / localScale).coerceIn(10f, 64f).sp
                                        val descFontSp  = (17f / localScale).coerceIn(8f, 48f).sp

                                        val titleStyle = TextStyle(color = item.getColor(), fontSize = titleFontSp)
                                        val descStyle  = TextStyle(color = item.getColor().copy(alpha = 0.8f), fontSize = descFontSp)

                                        val titleText = AnnotatedString(item.title)
                                        val descText = AnnotatedString(item.description)

                                        val titleLayout = textMeasurer.measure(text = titleText, style = titleStyle)
                                        val titleSize = titleLayout.size // IntSize (px)
                                        val titleW = titleSize.width.toFloat()
                                        val titleH = titleSize.height.toFloat()

                                        var descW = 0f
                                        var descH = 0f
                                        val hasDesc = item.description.isNotBlank()
                                        val descLayout = if (hasDesc) {
                                            textMeasurer.measure(text = descText, style = descStyle)
                                        } else null

                                        if (descLayout != null) {
                                            val ds = descLayout.size
                                            descW = ds.width.toFloat()
                                            descH = ds.height.toFloat()
                                        }

                                        val spacing = (4f / localScale)
                                        val totalHeight = titleH + (if (hasDesc) spacing + descH else 0f)
                                        val center = item.getCenter()
                                        val stackTopY = center.y - totalHeight / 2f
                                        val titleTopLeft = Offset(
                                            x = center.x - titleW / 2f,
                                            y = stackTopY
                                        )

                                        this.drawText(
                                            textMeasurer = textMeasurer,
                                            text = titleText,
                                            topLeft = titleTopLeft,
                                            style = titleStyle
                                        )

                                        if (hasDesc && descLayout != null) {
                                            val descTopLeft = Offset(
                                                x = center.x - descW,
                                                y = stackTopY + titleH + spacing
                                            )

                                            this.drawText(
                                                textMeasurer = textMeasurer,
                                                text = descText,
                                                topLeft = descTopLeft,
                                                style = descStyle
                                            )
                                        }


                                    }
                                }
                            }
                        }


                        println(state.gridspacing)

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