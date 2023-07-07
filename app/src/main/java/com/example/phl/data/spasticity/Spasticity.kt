package com.example.phl.data.spasticity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.phl.data.AbstractData
import com.example.phl.data.AbstractResultData
import java.time.LocalDateTime
import java.util.Date

@Entity(tableName = "spasticity")
class Spasticity(
    @PrimaryKey override val sessionId: String,
    override val score: Double,
    override val time: LocalDateTime = LocalDateTime.now(),
) : AbstractResultData {
}