package com.example.phl.data.mas

import com.example.phl.data.AbstractResultData
import java.time.LocalDateTime


data class MasTestResult (
    val passiveRangeOfMotion: Float,
    val maximumAngularDeceleration: Float,
    val maximumAngularDecelerationAngle : Float,
    val angularDecelerationToAngularVelocityRatio : Float,
    val maximumAngularVelocity: Float,
    val maximumAngularAcceleration: Float,
    val angularAccelerationToAngleSlope: Float,
    val angularDecelerationToAngleSlope: Float,
    val score: Double,
    )