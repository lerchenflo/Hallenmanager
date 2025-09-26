package com.lerchenflo.hallenmanager.presentation.homescreen.search

import com.lerchenflo.hallenmanager.domain.Item

data class SearchItem(
    val item: Item,
    val areaname: String
)