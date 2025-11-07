package com.lerchenflo.hallenmanager.mainscreen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@Entity(tableName = "areas")
class AreaDto(
    @PrimaryKey(autoGenerate = false)
    var id: String = "",
    var name: String,
    var description: String,
    var createdAt: String,
    var lastchangedAt: String,
    var lastchangedBy: String,
    var networkConnectionId: Long?

)