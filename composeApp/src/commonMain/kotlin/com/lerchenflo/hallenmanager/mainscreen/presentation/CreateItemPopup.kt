package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.mainscreen.domain.Item
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.sharedUi.ColorPicker
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_item_titletext
import hallenmanager.composeapp.generated.resources.cancel
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.done
import hallenmanager.composeapp.generated.resources.iteminfo
import hallenmanager.composeapp.generated.resources.layers
import hallenmanager.composeapp.generated.resources.name
import hallenmanager.composeapp.generated.resources.use_custom_color
import hallenmanager.composeapp.generated.resources.use_layer_color
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.emptyList
import kotlin.time.Clock

@Composable
fun CreateItemPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    //println("Popup for item: ${state.iteminfopopupItem}")

    var title by remember { mutableStateOf(state.iteminfopopupItem?.title ?: "") }
    var description by remember { mutableStateOf(state.iteminfopopupItem?.description ?: "") }

    var useCustomColor by remember { mutableStateOf(state.iteminfopopupItem?.color != null) }

    var color by remember { mutableStateOf<Color?>(state.iteminfopopupItem?.getCustomColor()) }

    var selectedLayers by remember { mutableStateOf(state.iteminfopopupItem?.layers ?: emptyList<Layer>()) }
    var expanded by remember { mutableStateOf(false) }



    AlertDialog(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(Res.string.iteminfo))

                if (state.iteminfopopupItem != null){
                    IconButton(
                        onClick = {onAction(MainScreenAction.OnMoveItemToShortAccess(item = state.iteminfopopupItem))},

                    ){
                        Icon(
                            imageVector = Icons.Rounded.Archive,
                            contentDescription = "Move item to short access"
                        )
                    }
                }

            }
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
                    label = {
                        Text(
                            stringResource(Res.string.name)
                        )},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(
                        stringResource(Res.string.desc)
                    ) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))


                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box{
                        TextButton(
                            onClick = { expanded = true },
                        ){
                            Text(
                                text = stringResource(Res.string.layers)
                            )
                        }


                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ){
                            state.availableLayers.forEach { layer ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = selectedLayers.contains(layer),
                                                onCheckedChange = {
                                                    if (!selectedLayers.contains(layer)){
                                                        selectedLayers += layer
                                                    }else {
                                                        selectedLayers -= layer
                                                    }
                                                }
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = layer.name,
                                                maxLines = 1
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (!selectedLayers.contains(layer)){
                                            selectedLayers += layer
                                        }else {
                                            selectedLayers -= layer
                                        }
                                        //expanded = false
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(Res.string.done)

                                    )
                                },
                                onClick = {
                                    expanded = false
                                }
                            )
                        }
                    }


                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (!useCustomColor) stringResource(Res.string.use_custom_color) else stringResource(Res.string.use_layer_color)
                    )

                    Spacer(Modifier.width(12.dp))

                    Switch(
                        checked = useCustomColor,
                        onCheckedChange = {newstate ->
                            useCustomColor = newstate

                            //If unchecked reset selected color
                            if (!newstate){
                                color = null
                            }
                        }
                    )
                }

                if (useCustomColor){
                    ColorPicker(
                        onSelectColor = {
                            color = it
                        }
                    )
                }

            }
        },
        confirmButton = {
            TextButton(onClick = {

                onAction(MainScreenAction.OnInfoDialogSave(
                    Item(
                        itemid = state.iteminfopopupItem?.itemid ?: 0L,
                        title = title,
                        description = description,
                        layers = selectedLayers,
                        cornerPoints = if (state.currentDrawingOffsets.isEmpty() && state.iteminfopopupItem != null) state.iteminfopopupItem.cornerPoints else state.currentDrawingOffsets,
                        color = color?.value?.toLong(),
                        areaId = state.iteminfopopupItem?.areaId ?: state.currentArea?.id ?: 0,
                        onArea = true,
                        createdAt = Clock.System.now(),
                        lastchangedAt = Clock.System.now(),
                        lastchangedBy = "",
                        serverId = state.currentArea?.networkConnectionId
                    )
                ))
            }) {
                Text(stringResource(Res.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onAction(MainScreenAction.OnInfoDialogDismiss)
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        onDismissRequest = {
            onAction(MainScreenAction.OnInfoDialogDismiss)
        }

    )
}