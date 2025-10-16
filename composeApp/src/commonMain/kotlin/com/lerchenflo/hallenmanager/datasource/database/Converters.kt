package com.lerchenflo.hallenmanager.datasource.database

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class Converters {
    @ExperimentalTime
    @TypeConverter
    fun fromInstant(value: Instant): String {
        return value.toString()
    }

    @ExperimentalTime
    @TypeConverter
    fun toInstant(date: String): Instant {
        return Instant.parse(date)
    }
}