package com.example.phl.data.ball

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "ball_test")
data class BallTest(
    val sessionId: String,
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
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val time: LocalDateTime = LocalDateTime.now()
    )
