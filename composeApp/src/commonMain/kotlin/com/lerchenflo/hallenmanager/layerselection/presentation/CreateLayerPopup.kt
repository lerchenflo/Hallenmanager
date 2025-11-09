package com.lerchenflo.hallenmanager.layerselection.presentation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.layerselection.domain.Layer
import com.lerchenflo.hallenmanager.sharedUi.ColorPicker
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.add_layer_titletext
import hallenmanager.composeapp.generated.resources.layerinfo
import hallenmanager.composeapp.generated.resources.name
import io.ktor.client.request.invoke
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random
import kotlin.time.Clock

@Composable
fun CreateLayerPopup(
    onDismiss: () -> Unit = {},
    onSave: (Layer) -> Unit = {},
    layer: Layer? = null,
    networkConnectionId: Long? = null
) {
    var title by remember { mutableStateOf(layer?.name ?: "") }

    var color by remember {
        mutableStateOf(layer?.color?.let { Color(it.toULong()) })
    }


    AlertDialog(
        title = {
            Text(text = stringResource(Res.string.layerinfo))
        },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.add_layer_titletext),
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



                ColorPicker(
                    onSelectColor = {
                        println("Color set: $it")
                        color = it
                    },

                )

            }
        },
        confirmButton = {
            TextButton(onClick = {

                val currentInstant = Clock.System.now().toEpochMilliseconds().toString()

                onSave(Layer(
                    layerid = layer?.layerid ?: "",
                    name = title,
                    sortId = 0,
                    shown = true,
                    color = color?.value?.toLong() ?: getRandomColor().value.toLong(), //Random color if no color is selected
                    networkConnectionId = networkConnectionId,
                    createdAt = currentInstant,
                    lastchangedAt = currentInstant,
                    lastchangedBy = "you"
                ))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text("Cancel")
            }
        },
        onDismissRequest = {
            onDismiss()
        }

    )
}

private fun getRandomColor() : Color {
    val red = Random.nextInt(0x00, 0xFF)
    val blue = Random.nextInt(0x00, 0xFF)
    val green = Random.nextInt(0x00, 0xFF)
    val alpha = Random.nextInt(0x00, 0xFF)

    return Color(
        red = red,
        blue = blue,
        green = green,
        alpha = alpha
    )
}