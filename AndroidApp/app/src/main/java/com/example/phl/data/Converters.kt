package com.example.phl.data

import androidx.room.TypeConverter
import com.example.phl.data.ball.BallTestRaw
import com.example.phl.data.mas.MasTestRaw
import java.time.LocalDate
import java.util.Date
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


object Converters {
    private val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
    private val formatterDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val gson = Gson()
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

    @TypeConverter
    fun stringToTwoDimensionalDoubleArray(value: String): Array<DoubleArray> {
        val type = object : TypeToken<Array<DoubleArray>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun twoDimensionalDoubleArrayToString(array: Array<DoubleArray>): String {
        return Gson().toJson(array)
    }

    @TypeConverter
    fun stringToHandedness(value: String): BallTestRaw.Companion.Handedness {
        return BallTestRaw.Companion.Handedness.valueOf(value)
    }

    @TypeConverter
    fun handednessToString(handedness: BallTestRaw.Companion.Handedness): String {
        return handedness.name
    }

    @TypeConverter
    fun stringToCurrentTask(value: String): BallTestRaw.Companion.CurrentTask {
        return BallTestRaw.Companion.CurrentTask.valueOf(value)
    }

    @TypeConverter
    fun currentTaskToString(currentTask: BallTestRaw.Companion.CurrentTask): String {
        return currentTask.name
    }

    @TypeConverter
    fun stageLabelToString(stageLabel: MasTestRaw.Companion.StageLabel): String {
        return stageLabel.name
    }

    @TypeConverter
    fun stringToStageLabel(value: String): MasTestRaw.Companion.StageLabel {
        return MasTestRaw.Companion.StageLabel.valueOf(value)
    }

}