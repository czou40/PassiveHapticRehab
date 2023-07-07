package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractResultData
import java.time.LocalDateTime

@Entity(tableName = "ball_test_result")
data class BallTestResult (
    @PrimaryKey
    override val sessionId: String,
    override val score: Double,
    override val time: LocalDateTime = LocalDateTime.now()
) : AbstractResultData {
}
