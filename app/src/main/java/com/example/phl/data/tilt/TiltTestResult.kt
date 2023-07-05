package com.example.phl.data.tilt

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tilt_test_result")
data class TiltTestResult (
    @PrimaryKey
    val sessionId: String,
    val score: Int,
    val time: LocalDateTime = LocalDateTime.now()
)
