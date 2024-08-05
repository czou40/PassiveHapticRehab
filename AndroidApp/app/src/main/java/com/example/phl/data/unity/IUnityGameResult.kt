package com.example.phl.data.unity

import androidx.room.PrimaryKey

interface IUnityGameResult {
    val id: Long
    val startTime: Long
    val endTime: Long
    val score: Double
    val maximizingScore: Double
    val minimizingScore: Double
}