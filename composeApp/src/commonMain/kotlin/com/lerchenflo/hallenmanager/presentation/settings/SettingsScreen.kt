package com.lerchenflo.hallenmanager.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Coronavirus
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lerchenflo.hallenmanager.presentation.homescreen.CreateItemPopup
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.settings
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

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

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ){
            items(state.availableLayers) { layer ->
                Text(
                    text = layer.name
                )
            }
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