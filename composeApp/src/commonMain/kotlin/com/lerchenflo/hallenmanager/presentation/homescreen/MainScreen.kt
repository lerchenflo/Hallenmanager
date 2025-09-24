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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.snapToGrid
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_area
import hallenmanager.composeapp.generated.resources.searchbarhint
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreenRoot(
    viewmodel: MainScreenViewmodel = koinViewModel<MainScreenViewmodel>()
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

    var localScale by remember { mutableStateOf(1f) }
    var localOffset by remember { mutableStateOf(Offset.Zero) }


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
                        //TODO: Navigate to settings
                    },
                ){
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Navigate to settings",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    ){
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
                        .fillMaxWidth()
                ) {

                    //Seach textfield
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = state.searchterm,
                        maxLines = 1,
                        onValueChange = { onAction(MainScreenAction.OnSearchtermChange(it)) }, //In da datenbank gits a suchfeature
                        placeholder = { Text(stringResource(Res.string.searchbarhint)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent
                        )
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
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                        .clipToBounds()
                ){



                    Canvas(
                        modifier = Modifier
                            .size(2000.dp)

                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, rotation ->

                                    /*
                                    val oldScale = localScale
                                    val newScale = (oldScale * zoom).coerceIn(0.5f, 8f)

                                    //println("Newscale: $newScale")
                                    val scaleChangeFactor = newScale / oldScale

                                    // Keep point under centroid stable and add pan (in screen px)
                                    val newOffset = Offset(
                                        x = centroid.x - scaleChangeFactor * (centroid.x - localOffset.x) + pan.x,
                                        y = centroid.y - scaleChangeFactor * (centroid.y - localOffset.y) + pan.y
                                    )

                                     */
                                    localOffset = localOffset + pan

                                    //onAction(MainScreenAction.OnZoom(1f, localOffset))
                                }
                            }


                            /*
                        .graphicsLayer {
                            // Important: use top-left as transform origin so our math matches
                            transformOrigin = TransformOrigin(0f, 0f)
                            translationX = localOffset.x
                            translationY = localOffset.y
                            scaleX = localScale
                            scaleY = localScale
                        }

                             */
                            // Separate pointerInput for taps/longpress so they don't steal events from transform detector
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = { raw -> println("tap at $raw") },
                                    onLongPress = { raw ->

                                        val contentPoint = (raw - localOffset) / localScale

                                        val snapped = snapToGrid(contentPoint, state.gridspacing)

                                        onAction(MainScreenAction.OnAddPoint(snapped))
                                    }
                                )
                            }

                    ) {

                        withTransform({
                            // apply the *local* transform (not VM-state, so it reflects current gestures)
                            translate(left = localOffset.x, top = localOffset.y)
                            scale(localScale, localScale)
                        }) {
                            drawRect(Color.Gray)

                            //Grid
                            var x = 0f
                            while (x <= size.width) {
                                drawLine(
                                    color = Color.Red,
                                    start = Offset(x, 0f),
                                    end = Offset(x, size.height),
                                    strokeWidth = 1f / localScale
                                )
                                x += state.gridspacing
                            }
                            var y = 0f
                            while (y <= size.height) {
                                drawLine(
                                    color = Color.Red,
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
                                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                                    )

                                    //Fill polygon with color
                                    drawPath(
                                        path = path,
                                        color = item.getColor().copy(alpha = 0.4f),
                                        style = Fill
                                    )
                                }
                            }
                        }
                    }



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