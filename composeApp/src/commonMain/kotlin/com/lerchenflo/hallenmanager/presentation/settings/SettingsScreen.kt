package com.lerchenflo.hallenmanager.presentation.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.lerchenflo.hallenmanager.presentation.homescreen.CreateItemPopup
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
private fun SettingsScreen(
    state: SettingsScreenState = SettingsScreenState(),
    onAction: (SettingsScreenAction) -> Unit = {}
){

    if (state.addlayerpopupshown) {
        CreateLayerPopup(
            onDismiss = {onAction(SettingsScreenAction.OnCreateLayerDismiss)},
            onSave = {onAction(SettingsScreenAction.OnCreateLayerSave(it))},
            layer = state.selectedLayerPopupLayer,
        )
    }



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
                onClick = { onAction(SettingsScreenAction.OnNavigateBack) },
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
                text = stringResource(Res.string.settings),
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
            onAction(SettingsScreenAction.OnLayerReorder(updatedList))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                                    onAction(SettingsScreenAction.OnLayerClick(layer))
                                },
                                modifier = Modifier.weight(1f)
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

        /*
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),

        ){
            items(
                items = state.availableLayers.sortedBy { it.sortId }
            ) { layer ->
                LayeritemUi(
                    layer = layer,
                    onClick = {
                        onAction(SettingsScreenAction.OnLayerClick(layer))
                    }
                )

                HorizontalDivider(modifier = Modifier.height(4.dp))


            }
        }

         */

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            onClick = {onAction(SettingsScreenAction.OnCreateLayerStart)},

            ){
            Text(
                text = "Add layer"
            )
        }


    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenRoot(
    viewmodel: SettingsScreenViewmodel
){
    SettingsScreen(
        state = viewmodel.state,
        onAction = viewmodel::onAction
    )
}