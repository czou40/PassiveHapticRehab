package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractData
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "ball_test")
data class BallTestRaw  (
    override val sessionId: String,
    val thumbAngle0: Double,
    val thumbAngle1: Double,
    val thumbAngle2: Double,
    val indexAngle0: Double,
    val indexAngle1: Double,
    val indexAngle2: Double,
    val middleAngle0: Double,
    val middleAngle1: Double,
    val middleAngle2: Double,
    val ringAngle0: Double,
    val ringAngle1: Double,
    val ringAngle2: Double,
    val pinkieAngle0: Double,
    val pinkieAngle1: Double,
    val pinkieAngle2: Double,
    override val time: LocalDateTime = LocalDateTime.now(),
    @PrimaryKey
    val id: String = UUID.randomUUID().toString()
    ) : AbstractData {
    fun getStrength(): Double {
        val thumbAverage = (thumbAngle0 + thumbAngle1 + thumbAngle2) / 3
        val indexAverage = (indexAngle0 + indexAngle1 + indexAngle2) / 3
        val middleAverage = (middleAngle0 + middleAngle1 + middleAngle2) / 3
        val ringAverage = (ringAngle0 + ringAngle1 + ringAngle2) / 3
        val pinkieAverage = (pinkieAngle0 + pinkieAngle1 + pinkieAngle2) / 3

        val average =
            (thumbAverage + indexAverage + middleAverage + ringAverage + pinkieAverage) / 5

        return ((180 - average) / 90 * 100)
    }
    fun getDescription(): String {
        val thumbAverage = (thumbAngle0 + thumbAngle1 + thumbAngle2) / 3
        val indexAverage = (indexAngle0 + indexAngle1 + indexAngle2) / 3
        val middleAverage = (middleAngle0 + middleAngle1 + middleAngle2) / 3
        val ringAverage = (ringAngle0 + ringAngle1 + ringAngle2) / 3
        val pinkieAverage = (pinkieAngle0 + pinkieAngle1 + pinkieAngle2) / 3

        val average =
            (thumbAverage + indexAverage + middleAverage + ringAverage + pinkieAverage) / 5

        val strength = ((180 - average) / 90 * 100)
        return "Thumb extension: ${thumbAverage.toInt()}\n" +
                "Index extension: ${indexAverage.toInt()}\n" +
                "Middle extension: ${middleAverage.toInt()}\n" +
                "Ring extension: ${ringAverage.toInt()}\n" +
                "Pinkie extension: ${pinkieAverage.toInt()}\n" +
                "Average: ${average.toInt()}\n" +
                "Strength: $strength%"
    }
}
