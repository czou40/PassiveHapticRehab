package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractResultData
import java.time.LocalDateTime
import java.time.ZonedDateTime

@Entity(tableName = "ball_test_result")
data class BallTestResult (
    @PrimaryKey
    override val sessionId: String,
    val closeHandTestResult: Double,
    val openHandTestResult: Double,
    override val score: Double,
    override val time: LocalDateTime = LocalDateTime.now()
) : AbstractResultData {
}
