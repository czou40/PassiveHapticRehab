package com.example.phl.data.spasticity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phl.data.AbstractDao
import java.time.LocalDate

@Dao
interface SpasticityDao : AbstractDao<Spasticity> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert(data: Spasticity)

    @Query("SELECT * FROM spasticity")
    override fun getAll(): List<Spasticity>

    @Query("SELECT * FROM spasticity where sessionId = :sessionId")
    override fun getBySessionId(sessionId: String): List<Spasticity>

    @Query("SELECT * FROM spasticity WHERE sessionId = :id")
    override fun getById(id: String): Spasticity

    @Query("SELECT * FROM spasticity WHERE strftime('%Y-%m-%d', time) = :day")
    override fun getByDay(day: LocalDate): List<Spasticity>

    @Query("DELETE FROM spasticity WHERE sessionId = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM spasticity WHERE sessionId = :sessionId")
    override fun deleteBySessionId(sessionId: String)
}