package com.lerchenflo.hallenmanager.datasource.remote

import androidx.room.Entity
import androidx.room.PrimaryKey

//TODO: Move to domain and make a data object

@Entity
data class NetworkConnection(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userName: String,
    val serverUrl: String,
    val alias: String
)
