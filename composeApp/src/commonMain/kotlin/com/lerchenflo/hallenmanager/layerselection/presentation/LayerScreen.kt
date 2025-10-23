package com.lerchenflo.hallenmanager.layerselection.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.layers
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
private fun LayerScreen(
    state: LayerScreenState = LayerScreenState(),
    onAction: (LayerScreenAction) -> Unit = {}
){

    if (state.addlayerpopupshown) {
        CreateLayerPopup(
            onDismiss = { onAction(LayerScreenAction.OnCreateLayerDismiss) },
            onSave = { onAction(LayerScreenAction.OnCreateLayerSave(it)) },
            layer = state.selectedLayerPopupLayer,
            networkConnectionId = state.selectedArea.networkConnectionId
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
        ,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(LayerScreenAction.OnCreateLayerStart)
                },
            ){
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "add layer",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
        ){

            //Title header
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onAction(LayerScreenAction.OnNavigateBack) },
                    modifier = Modifier
                        .padding(top = 5.dp, start = 5.dp)
                        .statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                    )
                }

                Text(
                    text = stringResource(Res.string.layers),
                    modifier = Modifier
                        .weight(1f)
                        .align(alignment = Alignment.CenterVertically)
                        .padding(start = 10.dp),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 20.sp,
                        maxFontSize = 30.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            val hapticFeedback = LocalHapticFeedback.current
            val lazyListState = rememberLazyListState()

            val sortedLayers = state.availableLayers.sortedByDescending { it.sortId }

            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                val newlist = sortedLayers.toMutableList().apply {
                    add(to.index, removeAt(from.index))
                }
                // Update sortId for each layer based on new position
                val updatedList = newlist.mapIndexed { index, layer ->
                    layer.copy(sortId = newlist.size - 1 - index)
                }
                onAction(LayerScreenAction.OnLayerReorder(updatedList))
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = lazyListState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = sortedLayers,
                    key = { it.layerid }
                ) { layer ->
                    ReorderableItem(reorderableLazyListState, key = layer.layerid) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                        Surface(
                            shadowElevation = elevation,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LayeritemUi(
                                    layer = layer,
                                    onClick = {
                                        onAction(LayerScreenAction.OnLayerClick(layer))
                                    },
                                    modifier = Modifier.weight(1f),
                                    onVisibilityClick = {
                                        onAction(LayerScreenAction.OnLayerVisibilityChange(layer, it))
                                    }
                                )

                                IconButton(
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        },
                                    ),
                                    onClick = {},
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DragHandle,
                                        contentDescription = "Reorder",
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }




        }
    }
}

@Preview(showBackground = true)
@Composable
fun LayerScreenRoot(
    viewmodel: LayerScreenViewmodel
){
    LayerScreen(
        state = viewmodel.state,
        onAction = viewmodel::onAction
    )
}