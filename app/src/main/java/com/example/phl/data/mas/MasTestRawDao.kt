package com.example.phl.data.mas

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phl.data.AbstractDao
import java.time.LocalDate

interface MasTestRawDao: AbstractDao<MasTestRaw> {

    @Query("SELECT * FROM mas_test")
    override fun getAll(): List<MasTestRaw>

    @Query("SELECT * FROM mas_test WHERE id = :id")
    override fun getById(id: String): MasTestRaw

    @Query("SELECT * FROM mas_test WHERE sessionId = :sessionId")
    override fun getBySessionId(sessionId: String): List<MasTestRaw>

    @Query("SELECT * FROM mas_test WHERE strftime('%Y-%m-%d', time) = :day")
    override fun getByDay(day: LocalDate): List<MasTestRaw>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert(data: MasTestRaw)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(data: List<MasTestRaw>)

    @Query("DELETE FROM mas_test WHERE id = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM mas_test WHERE sessionId = :sessionId")
    override fun deleteBySessionId(sessionId: String)
}