package com.lerchenflo.hallenmanager.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.Layer

@Composable
fun LayeritemUi(
    layer: Layer,
    onClick : () -> Unit,
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .clickable{
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically

    ){

        Text(
            modifier = Modifier.weight(1f),
            text = layer.name
        )


        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 24.dp)
                .background(color = layer.getColor(), shape = RoundedCornerShape(4.dp))
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(4.dp))
        )

    }
}