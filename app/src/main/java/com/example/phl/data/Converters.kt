package com.example.phl.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.util.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Converters {
    private val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    private val formatterDate = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatterDateTime) }
    }

    @TypeConverter
    fun localDateTimeToString(date: LocalDateTime?): String? {
        return date?.format(formatterDateTime)
    }

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it, formatterDate) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.format(formatterDate)
    }
}