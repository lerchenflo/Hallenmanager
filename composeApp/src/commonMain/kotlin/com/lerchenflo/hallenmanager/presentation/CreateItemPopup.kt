package com.lerchenflo.hallenmanager.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.Item
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_item_titletext
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.iteminfo
import hallenmanager.composeapp.generated.resources.name
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateItemPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedLayer by remember { mutableStateOf(state.availableLayers.first()) } //TODO: Mehrere layer glichzittig uswähla
    var expanded by remember { mutableStateOf(false) }


    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.iteminfo))
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
                Box{
                    TextButton(
                        onClick = { expanded = true },
                    ){
                        Text(
                            text = selectedLayer.name
                        )
                    }


                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ){
                        state.availableLayers.forEach { layer ->
                            DropdownMenuItem(
                                text = { Text(layer.name) },
                                onClick = {
                                    selectedLayer = layer
                                    expanded = false
                                }
                            )
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
                        layer = selectedLayer.name,
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