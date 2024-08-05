package com.example.phl.data.unity

import androidx.room.Insert

interface BaseUnityGameResultDao<T:IUnityGameResult> {
    @Insert
    suspend fun insert(result: T): Long
}