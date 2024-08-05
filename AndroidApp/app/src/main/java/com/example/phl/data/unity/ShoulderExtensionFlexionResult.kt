package com.example.phl.data.unity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shoulder_extension_flexion_result")
data class ShoulderExtensionFlexionResult(
    val minAngles: List<Double>,
    val maxAngles: List<Double>,
    override val startTime: Long, // Assuming start time is in milliseconds
    override val endTime: Long, // Assuming end time is in milliseconds
    @PrimaryKey(autoGenerate = true)
    override val id: Long = -1,
) : IUnityGameResult {

    init {
        // Ensure minimumAngles and maximumAngles are the same size
        require(minAngles.size == maxAngles.size) { "minimumAngles and maximumAngles must be of the same size" }
    }

    val numRounds: Int
        get() = minAngles.size

    val averageMinAngle: Double
        get() = minAngles.average()

    val averageMaxAngle: Double
        get() = maxAngles.average()

    val rangeOfMotion: Double
        get() = averageMaxAngle - averageMinAngle

    override val score: Double
        get() = rangeOfMotion

    override val maximizingScore: Double
        get() = averageMaxAngle

    override val minimizingScore: Double
        get() = averageMinAngle
}
