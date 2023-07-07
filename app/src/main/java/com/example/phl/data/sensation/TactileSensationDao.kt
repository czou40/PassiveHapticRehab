package com.example.phl.data.sensation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phl.data.AbstractDao
import java.time.LocalDate

@Dao
interface TactileSensationDao : AbstractDao<TactileSensation> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert(data: TactileSensation)

    @Query("SELECT * FROM tactile_sensation")
    override fun getAll(): List<TactileSensation>

    @Query("SELECT * FROM tactile_sensation where sessionId = :sessionId")
    override fun getBySessionId(sessionId: String): List<TactileSensation>

    @Query("SELECT * FROM tactile_sensation WHERE sessionId = :id")
    override fun getById(id: String): TactileSensation

    @Query("SELECT * FROM tactile_sensation WHERE strftime('%Y-%m-%d', time) = :day")
    override fun getByDay(day: LocalDate): List<TactileSensation>

    @Query("DELETE FROM tactile_sensation WHERE sessionId = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM tactile_sensation WHERE sessionId = :sessionId")
    override fun deleteBySessionId(sessionId: String)
}