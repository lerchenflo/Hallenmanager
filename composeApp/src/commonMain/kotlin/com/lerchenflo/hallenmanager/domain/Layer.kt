package com.lerchenflo.hallenmanager.domain

data class Layer(
    val id: Long,
    val name: String,
    val sortId: Int,
    val shown: Boolean
)