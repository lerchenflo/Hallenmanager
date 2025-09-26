package com.lerchenflo.hallenmanager.presentation.homescreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.Item

@Composable
fun SearchItemUI(
    item: Item,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{
                onClick()
            }
    ) {
        Column {
            Text(
                text = item.title,
                maxLines = 1
            )

            Text(
                text = item.description,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                maxLines = 1
            )
        }


    }
}