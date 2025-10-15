@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
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
import com.lerchenflo.hallenmanager.mainscreen.domain.Area
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_area_titletext
import hallenmanager.composeapp.generated.resources.areainfo
import hallenmanager.composeapp.generated.resources.cancel
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.name
import hallenmanager.composeapp.generated.resources.save
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun CreateAreaPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }


    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.areainfo))
        },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.add_area_titletext),
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
            }
        },
        confirmButton = {
            TextButton(onClick = {

                onAction(MainScreenAction.OnAreaDialogSave(
                    Area(
                        name = title,
                        description = description,
                        items = emptyList(),
                        createdAt = Clock.System.now(),
                        lastchangedAt = Clock.System.now(),
                        lastchangedBy = "",
                    )
                ))
            }) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onAction(MainScreenAction.OnAreaDialogDismiss)
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        onDismissRequest = {
            onAction(MainScreenAction.OnAreaDialogDismiss)
        }

    )
}