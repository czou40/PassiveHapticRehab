package com.example.phl.data.tilt

import androidx.room.*
import com.example.phl.data.AbstractDao
import java.time.LocalDate

@Dao
interface TiltTestResultDao : AbstractDao<TiltTestResult> {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        override fun insert(data: TiltTestResult)

        @Query("SELECT * FROM tilt_test_result")
        override fun getAll(): List<TiltTestResult>

        @Query("SELECT * FROM tilt_test_result where sessionId = :sessionId")
        override fun getBySessionId(sessionId: String): List<TiltTestResult>

        @Query("SELECT * FROM tilt_test_result WHERE sessionId = :id")
        override fun getById(id: String): TiltTestResult

        @Query("SELECT * FROM tilt_test_result WHERE strftime('%Y-%m-%d', time) = :day")
        override fun getByDay(day: LocalDate): List<TiltTestResult>

        @Query("DELETE FROM tilt_test_result WHERE sessionId = :id")
        override fun deleteById(id: String)

        @Query("DELETE FROM tilt_test_result WHERE sessionId = :sessionId")
        override fun deleteBySessionId(sessionId: String)
}
