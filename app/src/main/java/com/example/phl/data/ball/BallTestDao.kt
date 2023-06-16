package com.example.phl.data.ball

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface BallTestDao {

    @Insert
    fun insert(ballTest: BallTest): Long

    @Query("SELECT * FROM ball_test")
    fun getAllBallTests(): List<BallTest>

    @Query("SELECT * FROM ball_test where sessionId = :sessionId")
    fun getBallTestsBySessionId(sessionId: String): List<BallTest>

    @Query("SELECT * FROM ball_test WHERE id = :id")
    fun getBallTestById(id: Int): BallTest

    @Query("DELETE FROM ball_test WHERE id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM ball_test WHERE sessionId = :sessionId")
    fun deleteBySessionId(sessionId: String)
}
