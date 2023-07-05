package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "ball_test_result")
data class BallTestResult (
    @PrimaryKey
    val sessionId: String,
    val score: Int,
    val time: LocalDateTime = LocalDateTime.now()
)
