package com.example.phl.data.tilt

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractResultData
import java.time.LocalDateTime

@Entity(tableName = "tilt_test_result")
data class TiltTestResult (
    @PrimaryKey
    override val sessionId: String,
    override val score: Double,
    override val time: LocalDateTime = LocalDateTime.now()
) : AbstractResultData {
}
