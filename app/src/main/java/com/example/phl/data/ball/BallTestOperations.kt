package com.example.phl.data.ball

import android.content.Context
import com.example.phl.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class BallTestOperations {
    companion object {
        suspend fun insertRawData(context: Context, ballTest: BallTest) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().insert(ballTest)
            }
        }

        suspend fun loadRawData(context: Context): List<BallTest> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().getAllBallTests()
            }
        }

        suspend fun loadRawData(context: Context, sessionId: String): List<BallTest> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().getBallTestsBySessionId(sessionId)
            }
        }

        suspend fun deleteRawData(context: Context, id: Int) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().delete(id)
            }
        }

        suspend fun deleteRawData(context: Context, sessionId: String) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().deleteBySessionId(sessionId)
            }
        }

        suspend fun insertResultData(context: Context, ballTestResult: BallTestResult) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestResultDao().insert(ballTestResult)
            }
        }

        suspend fun loadResultData(context: Context): List<BallTestResult> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestResultDao().getAll()
            }
        }

        suspend fun loadResultDataByDay(context: Context, day: LocalDate): List<BallTestResult> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestResultDao().findByDay(day)
            }
        }

        suspend fun deleteResultData(context: Context, sessionId: String) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestResultDao().deleteBySessionId(sessionId)
            }
        }
    }
}
