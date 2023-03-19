package com.example.phl.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.phl.data.sensation.TactileSensation;
import com.example.phl.data.sensation.TactileSensationDao;

import java.util.Date;
import java.util.List;

@Database(entities = {TactileSensation.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase db;

    public abstract TactileSensationDao tactileSensationDao();

    public static AppDatabase getInstance(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(context, AppDatabase.class, "tactile_sensation_database").build();
        }
        return db;
    }

    public static interface OnDataLoadedListener {
        void onDataLoaded(List<TactileSensation> tactileSensations);
    }
}
