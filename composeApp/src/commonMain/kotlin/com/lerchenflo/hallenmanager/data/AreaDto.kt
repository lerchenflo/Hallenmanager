@file:OptIn(ExperimentalTime::class)

package com.lerchenflo.hallenmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@Entity(tableName = "areas")
class AreaDto(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,
    var name: String,
    var description: String,
    var createdAt: Instant,
    var lastchangedAt: Instant,
    var lastchangedBy: String,
)