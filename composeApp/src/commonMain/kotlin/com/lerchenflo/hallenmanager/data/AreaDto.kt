package com.lerchenflo.hallenmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "areas")
class AreaDto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    var name: String,
    var description: String,

) {
}