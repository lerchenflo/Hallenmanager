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
import hallenmanager.composeapp.generated.resources.add_connection_subtext
import hallenmanager.composeapp.generated.resources.add_connection_title
import hallenmanager.composeapp.generated.resources.areainfo
import hallenmanager.composeapp.generated.resources.cancel
import hallenmanager.composeapp.generated.resources.connection_alias
import hallenmanager.composeapp.generated.resources.connection_username
import hallenmanager.composeapp.generated.resources.desc
import hallenmanager.composeapp.generated.resources.name
import hallenmanager.composeapp.generated.resources.save
import hallenmanager.composeapp.generated.resources.serverurl
import hallenmanager.composeapp.generated.resources.serverurl_placeholder
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun AddConnectionPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    var serverurl by remember { mutableStateOf("http://") }
    var username by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }


    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.add_connection_title))
        },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.add_connection_subtext),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input field
                OutlinedTextField(
                    value = serverurl,
                    onValueChange = { serverurl = it },
                    label = { Text(stringResource(Res.string.serverurl)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(Res.string.serverurl_placeholder)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(Res.string.connection_username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text(stringResource(Res.string.connection_alias)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {

                onAction(MainScreenAction.CreateConnection(
                    serverurl = serverurl,
                    userName = username,
                    alias = alias
                ))


            }) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onAction(MainScreenAction.OnCreateConnectionStop)
            }) {
                Text(stringResource(Res.string.cancel))
            }
        },
        onDismissRequest = {
            onAction(MainScreenAction.OnCreateConnectionStop)
        }

    )
}