package com.lerchenflo.hallenmanager.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.domain.Line
import com.lerchenflo.hallenmanager.domain.snapToGrid
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_item_titletext
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.name
import hallenmanager.composeapp.generated.resources.searchbarhint
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.exp
import kotlin.math.roundToInt

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

    if (state.infopopupshown) {

        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }


        val layers = listOf("Layer 1", "Layer 2", "Layer 3")
        var selectedLayer by remember { mutableStateOf(layers.first()) }
        var expanded by remember { mutableStateOf(false) }


        AlertDialog(
            title = {
                Text(text = "Info")
            },
            text = {
                Column {
                    Text(
                        text = stringResource(Res.string.add_item_titletext),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(Res.string.name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(Res.string.desc)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))


                    // Layer dropdown (ExposedDropdownMenu)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {

                        Box{
                            TextButton(
                                onClick = { expanded = true },
                            ){
                                Text(
                                    text = selectedLayer
                                )
                            }


                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                            ){
                                layers.forEach { layer ->
                                    DropdownMenuItem(
                                        text = { Text(layer) },
                                        onClick = {
                                            selectedLayer = layer
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }


                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {


                    onAction(MainScreenAction.OnInfoDialogSave(
                        Item(
                            title = title,
                            description = description,
                            layer = selectedLayer,
                            cornerPoints = state.currentDrawingOffsets
                        )
                    ))
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onAction(MainScreenAction.OnInfoDialogDismiss)
                }) {
                    Text("Cancel")
                }
            },
            onDismissRequest = {
                onAction(MainScreenAction.OnInfoDialogDismiss)
            }

        )
    }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ){

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

            Switch(
                checked = state.isDrawing,
                onCheckedChange = { onAction(MainScreenAction.OnSliderToggle(it)) },
            )
            //TODO: Layer dropdown
        }


        //Body main canvas
        Box(
            modifier = Modifier
                .size(2000.dp)
                .weight(1f)
                .clipToBounds()

        ){

            var localScale by remember { mutableStateOf(state.scale) }
            var localOffset by remember { mutableStateOf(state.offset) }






            Canvas(
                modifier = Modifier
                    .fillMaxSize()

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

                            onAction(MainScreenAction.OnZoom(1f, localOffset))
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
                    .pointerInput(state.isDrawing) {
                        detectTapGestures(
                            onTap = { raw -> println("tap at $raw") },
                            onLongPress = { raw ->

                                if (state.isDrawing){
                                    val contentPoint = (raw - localOffset) / state.scale

                                    val snapped = snapToGrid(contentPoint, state.gridspacing)

                                    onAction(MainScreenAction.OnAddPoint(snapped))
                                }
                            }
                        )
                    }

            ) {

                withTransform({
                    // apply the *local* transform (not VM-state, so it reflects current gestures)
                    translate(left = state.offset.x, top = state.offset.y)
                    scale(state.scale, state.scale)
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


                    //Draw items in this area
                    state.currentArea.items.forEach { item ->
                        println("Item print: ${item.id} ${item.cornerPoints.size}")

                        if (item.cornerPoints.size > 2){
                            val path = Path().apply {
                                fillType = PathFillType.NonZero // or EvenOdd depending on desired winding
                                moveTo(item.cornerPoints[0].x, item.cornerPoints[0].y)
                                for (i in 1 until item.cornerPoints.size) {
                                    lineTo(item.cornerPoints[i].x, item.cornerPoints[i].y)
                                }
                                close() // close the polygon
                            }

                            // fill the polygon (semi-transparent so you still see underlying grid)
                            drawPath(
                                path = path,
                                color = Color(0xFF90CAF9).copy(alpha = 0.5f), // pick color & alpha you like
                                style = Fill
                            )

                            // optional: stroke the outline on top
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )
                        }


                    }
                }
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