package com.example.phl.data.ball

import androidx.room.*
import java.time.LocalDate

@Dao
interface BallTestResultDao {

    @Query("SELECT * FROM ball_test_result")
    suspend fun getAll(): List<BallTestResult>

    @Query("SELECT * FROM ball_test_result WHERE sessionId = :sessionId")
    suspend fun findBySessionId(sessionId: String): BallTestResult

    @Query("SELECT * FROM ball_test_result WHERE strftime('%Y-%m-%d', time) = :day")
    suspend fun findByDay(day: LocalDate): List<BallTestResult>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ballTestResult: BallTestResult)

    @Update
    suspend fun update(ballTestResult: BallTestResult)

    @Delete
    suspend fun delete(ballTestResult: BallTestResult)

    @Query("DELETE FROM ball_test_result WHERE sessionId = :sessionId")
    suspend fun deleteBySessionId(sessionId: String)
}
