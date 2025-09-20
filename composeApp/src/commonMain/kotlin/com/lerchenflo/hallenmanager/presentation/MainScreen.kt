package com.lerchenflo.hallenmanager.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import hallenmanager.composeapp.generated.resources.Res
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



@Composable
fun MainScreen(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {

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

            //TODO: Layer dropdown
        }


        //Body main canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){

            var localScale by remember { mutableStateOf(state.scale) }
            var localOffset by remember { mutableStateOf(state.offset) }

            LaunchedEffect(state.scale, state.offset) {
                localScale = state.scale
                localOffset = state.offset
            }

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    // Transform gestures (pinch/drag) first so multi-touch is handled well
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            val oldScale = localScale
                            val newScale = (oldScale * zoom).coerceIn(0.5f, 8f)
                            val scaleChangeFactor = newScale / oldScale

                            // Keep point under centroid stable and add pan (in screen px)
                            val newOffset = Offset(
                                x = centroid.x - scaleChangeFactor * (centroid.x - localOffset.x) + pan.x,
                                y = centroid.y - scaleChangeFactor * (centroid.y - localOffset.y) + pan.y
                            )

                            localScale = newScale
                            localOffset = newOffset

                            onAction(MainScreenAction.OnZoom(newScale, newOffset))
                        }
                    }
                    .graphicsLayer {
                        // Important: use top-left as transform origin so our math matches
                        transformOrigin = TransformOrigin(0f, 0f)
                        translationX = localOffset.x
                        translationY = localOffset.y
                        scaleX = localScale
                        scaleY = localScale
                    }
                    // Separate pointerInput for taps/longpress so they don't steal events from transform detector
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { raw -> println("tap at $raw") },
                            onLongPress = { raw -> println("long at $raw") }
                        )
                    }
            ) {
                withTransform({
                    // apply the *local* transform (not VM-state, so it reflects current gestures)
                    translate(left = localOffset.x, top = localOffset.y)
                    scale(localScale, localScale)
                }) {
                    // Background + grid (example)
                    drawRect(Color.Gray)

                    val spacingPx = 40f

                    println("Line spacing: ${size.width / localScale} height ${size.height}")
                    var x = 0f
                    while (x <= size.width) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 1f / localScale
                        )
                        x += spacingPx
                    }
                    var y = 0f
                    while (y <= size.height) {
                        drawLine(
                            color = Color.Red,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1f / localScale
                        )
                        y += spacingPx
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