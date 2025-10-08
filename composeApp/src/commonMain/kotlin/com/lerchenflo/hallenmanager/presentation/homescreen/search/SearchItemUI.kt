package com.lerchenflo.hallenmanager.presentation.homescreen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lerchenflo.hallenmanager.domain.Item

@Composable
fun SearchItemUI(
    searchItem: SearchItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable{
                onClick()
            }
            .padding(8.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .padding(
                horizontal = 8.dp,
                vertical = 1.dp
            )



    ) {
        Column(
            modifier = Modifier.weight(1f),
            ) {
            Text(
                text = searchItem.item.title,
                maxLines = 1
            )

            Text(
                text = searchItem.item.description,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                maxLines = 1
            )
        }

        Text(
            text = searchItem.areaname
        )


    }
}