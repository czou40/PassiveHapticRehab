package com.example.phl.data.unity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface ShoulderExtensionFlexionResultDao: BaseUnityGameResultDao<ShoulderExtensionFlexionResult> {

    @Insert
    override suspend fun insert(result: ShoulderExtensionFlexionResult): Long

    @Insert
    suspend fun insertAll(results: List<ShoulderExtensionFlexionResult>): List<Long>

    @Update
    suspend fun update(result: ShoulderExtensionFlexionResult)

    @Delete
    suspend fun delete(result: ShoulderExtensionFlexionResult)

    @Query("SELECT * FROM shoulder_extension_flexion_result WHERE id = :id")
    suspend fun getById(id: Long): ShoulderExtensionFlexionResult?

    @Query("SELECT * FROM shoulder_extension_flexion_result")
    suspend fun getAll(): List<ShoulderExtensionFlexionResult>
}
