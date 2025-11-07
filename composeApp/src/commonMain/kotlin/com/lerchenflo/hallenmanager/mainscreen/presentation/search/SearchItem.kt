package com.lerchenflo.hallenmanager.mainscreen.presentation.search

import com.lerchenflo.hallenmanager.mainscreen.domain.Item

data class SearchItem(
    val item: Item,
    val areaname: String
)