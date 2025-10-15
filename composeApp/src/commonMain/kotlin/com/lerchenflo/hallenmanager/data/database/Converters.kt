package com.lerchenflo.hallenmanager.data.database

import androidx.room.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class Converters {
    @ExperimentalTime
    @TypeConverter
    fun fromInstant(value: Instant): String {
        println("Converter: ${value.toString()}")
        return value.toString()
    }

    @ExperimentalTime
    @TypeConverter
    fun toInstant(date: String): Instant {
        return Instant.parse(date)
    }
}