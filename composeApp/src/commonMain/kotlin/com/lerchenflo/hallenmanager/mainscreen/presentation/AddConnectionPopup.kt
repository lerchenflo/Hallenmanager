@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.mainscreen.presentation

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiFind
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.datasource.remote.NetworkUtils
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
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private sealed class CheckStatus {
    object Idle : CheckStatus()
    object Loading : CheckStatus()
    object Success : CheckStatus()
    object Error : CheckStatus()
}


@Preview
@Composable
fun AddConnectionPopup(
    onAction: (MainScreenAction) -> Unit = {},
    state : MainScreenState = MainScreenState()
) {
    var serverurl by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }


    val coroutinescope = rememberCoroutineScope()
    val networkUtils: NetworkUtils = koinInject<NetworkUtils>()
    var checkStatus by remember { mutableStateOf<CheckStatus>(CheckStatus.Idle) }

    val testServer: () -> Unit = {
        if (checkStatus != CheckStatus.Loading){
            checkStatus = CheckStatus.Loading
            coroutinescope.launch {
                if (!serverurl.startsWith("http://")){
                    serverurl = serverurl.prependIndent("https://") //Default https
                }

                val success = networkUtils.testServer(serverurl)

                checkStatus = if (success){
                    CheckStatus.Success
                } else CheckStatus.Error
            }
        }
    }

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = serverurl,
                        onValueChange = {
                            serverurl = it
                            checkStatus = CheckStatus.Idle
                                        },
                        label = { Text(stringResource(Res.string.serverurl)) },
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = stringResource(Res.string.serverurl_placeholder)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        isError = (checkStatus == CheckStatus.Error),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                testServer()
                            }
                        )
                        )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            testServer()
                        }
                    ) {

                        when (checkStatus) {
                            CheckStatus.Idle -> Icon(
                                imageVector = Icons.Default.WifiFind,
                                contentDescription = "Test"
                            )
                            CheckStatus.Loading -> CircularProgressIndicator(
                                modifier = Modifier.height(20.dp)
                            )
                            CheckStatus.Success -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "OK"
                            )
                            is CheckStatus.Error -> Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null
                            )
                        }

                    }
                }



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