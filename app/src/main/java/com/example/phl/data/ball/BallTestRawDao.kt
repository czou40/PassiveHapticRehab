package com.example.phl.data.ball

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phl.data.AbstractDao
import java.time.LocalDate

@Dao
interface BallTestRawDao : AbstractDao<BallTestRaw> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override fun insert(data: BallTestRaw)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(data: List<BallTestRaw>)

    @Query("SELECT * FROM ball_test")
    override fun getAll(): List<BallTestRaw>

    @Query("SELECT * FROM ball_test where sessionId = :sessionId")
    override fun getBySessionId(sessionId: String): List<BallTestRaw>

    @Query("SELECT * FROM ball_test WHERE id = :id")
    override fun getById(id: String): BallTestRaw

    @Query("SELECT * FROM ball_test WHERE strftime('%Y-%m-%d', time) = :day")
    override fun getByDay(day: LocalDate): List<BallTestRaw>

    @Query("DELETE FROM ball_test WHERE id = :id")
    override fun deleteById(id: String)

    @Query("DELETE FROM ball_test WHERE sessionId = :sessionId")
    override fun deleteBySessionId(sessionId: String)

    @Query("DELETE FROM ball_test WHERE sessionId = :sessionId AND currentTask = :currentTask")
    fun deleteBySessionIdAndCurrentTask(sessionId: String, currentTask: BallTestRaw.Companion.CurrentTask)
}
