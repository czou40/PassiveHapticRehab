package com.example.phl.data.ball

import android.content.Context
import com.example.phl.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BallTestOperations {
    companion object {
        suspend fun insertData(context: Context, ballTest: BallTest) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().insert(ballTest)
            }
        }

        suspend fun loadData(context: Context): List<BallTest> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().getAllBallTests()
            }
        }

        suspend fun loadData(context: Context, sessionId: String): List<BallTest> {
            return withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().getBallTestsBySessionId(sessionId)
            }
        }

        suspend fun deleteData(context: Context, id: Int) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().delete(id)
            }
        }

        suspend fun deleteData(context: Context, sessionId: String) {
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(context)
                db.ballTestDao().deleteBySessionId(sessionId)
            }
        }
    }
}
