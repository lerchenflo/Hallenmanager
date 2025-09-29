package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.lerchenflo.hallenmanager.domain.Item
import com.lerchenflo.hallenmanager.presentation.ColorPicker
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_item_titletext
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.done
import hallenmanager.composeapp.generated.resources.iteminfo
import hallenmanager.composeapp.generated.resources.layers
import hallenmanager.composeapp.generated.resources.name
import hallenmanager.composeapp.generated.resources.use_custom_color
import hallenmanager.composeapp.generated.resources.use_layer_color
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateItemPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var useCustomColor by remember { mutableStateOf(false) }

    var color by remember { mutableStateOf<Color?>(null) }


    var selectedLayer by remember { mutableStateOf(state.availableLayers) }
    var expanded by remember { mutableStateOf(false) }



    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.iteminfo))
        },
        text = {
            Column {
                Text(
                    text = state.iteminfopopupItem?.title ?: stringResource(Res.string.add_item_titletext),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            state.iteminfopopupItem?.title ?: stringResource(Res.string.name)
                        )},
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(
                        state.iteminfopopupItem?.description ?: stringResource(Res.string.desc)
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
                                                checked = layer.shown,
                                                onCheckedChange = {
                                                    layer.shown = !layer.shown
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
                                        layer.shown = !layer.shown
                                        //expanded = false
                                    }
                                )
                            }
                            //TODO: Add layer popup
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
                        title = title,
                        description = description,
                        layer = selectedLayer,
                        cornerPoints = state.currentDrawingOffsets,
                        color = color?.value?.toLong(),
                        areaId = state.currentArea?.id ?: 0
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