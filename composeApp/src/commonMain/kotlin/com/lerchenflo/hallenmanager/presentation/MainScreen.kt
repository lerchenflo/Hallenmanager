package com.lerchenflo.hallenmanager.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import hallenmanager.composeapp.generated.resources.Res
import hallenmanager.composeapp.generated.resources.searchbarhint
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreenRoot(
    viewmodel: MainScreenViewmodel = koinViewModel<MainScreenViewmodel>()
){
    MainScreen(
        state = viewmodel.state,
        onAction = viewmodel::onAction
    )
}



@Composable
fun MainScreen(
    state: MainScreenState,
    onAction: (MainScreenAction) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
    ){

        //Titlerow
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.searchterm,
                maxLines = 1,
                onValueChange = { onAction(MainScreenAction.onsearchtermChange(it)) }, //In da datenbank gits a suchfeature
                modifier = Modifier
                    .weight(1f),
                placeholder = { Text(stringResource(Res.string.searchbarhint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }


        //Body main canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ){
            Canvas(
                modifier = Modifier
                    .fillMaxSize() //fill max size inside box
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                println("short click: $offset")
                            },
                            onLongPress = { offset ->
                                println("long click: $offset")
                            }
                        )
                    }
            ){
                //Canvas body
                drawRect(Color.Gray)
            }



        }


    }

}