package com.example.phl.data.ball

import androidx.room.*
import com.example.phl.data.AbstractDao
import java.time.LocalDate

@Dao
interface BallTestResultDao: AbstractDao<BallTestResult> {

    @Query("SELECT * FROM ball_test_result")
    override fun getAll(): List<BallTestResult>

    @Query("SELECT * FROM ball_test_result WHERE sessionId = :id")
    override fun getById(id: String): BallTestResult

    @Query("SELECT * FROM ball_test_result WHERE sessionId = :sessionId")
    override fun getBySessionId(sessionId: String): List<BallTestResult>

    @Query("SELECT * FROM ball_test_result WHERE strftime('%Y-%m-%d', time) = :day")
    override fun getByDay(day: LocalDate): List<BallTestResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert(data: BallTestResult)

    @Query("DELETE FROM ball_test_result WHERE sessionId = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM ball_test_result WHERE sessionId = :sessionId")
    override fun deleteBySessionId(sessionId: String)
}
