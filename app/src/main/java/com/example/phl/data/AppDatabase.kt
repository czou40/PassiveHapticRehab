package com.example.phl.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.phl.data.ball.BallTestRaw
import com.example.phl.data.ball.BallTestRawDao
import com.example.phl.data.ball.BallTestResult
import com.example.phl.data.ball.BallTestResultDao
import com.example.phl.data.sensation.TactileSensation
import com.example.phl.data.sensation.TactileSensationDao
import com.example.phl.data.spasticity.Spasticity
import com.example.phl.data.spasticity.SpasticityDao
import com.example.phl.data.tilt.TiltTestResult
import com.example.phl.data.tilt.TiltTestResultDao

@Database(
    entities = [TactileSensation::class, Spasticity::class, BallTestRaw::class, BallTestResult::class, TiltTestResult::class],
    version = 7
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tactileSensationDao(): TactileSensationDao
    abstract fun spasticityDao(): SpasticityDao
    abstract fun ballTestRawDao(): BallTestRawDao
    abstract fun ballTestResultDao(): BallTestResultDao
    abstract fun tiltTestResultDao(): TiltTestResultDao

    companion object {
        private var db: AppDatabase?= null
        fun getInstance(context: Context?): AppDatabase {
            if (db == null) {
                db = databaseBuilder(
                    context!!,
                    AppDatabase::class.java,
                    "tactile_sensation_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return db!!
        }
    }
}