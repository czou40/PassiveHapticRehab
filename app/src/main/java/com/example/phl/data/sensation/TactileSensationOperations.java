package com.example.phl.data.sensation;

import android.content.Context;
import android.os.AsyncTask;

import com.example.phl.data.AppDatabase;

import java.util.Date;
import java.util.List;

public class TactileSensationOperations {

    public static void insertData(Context context, Date date, int value) {
        TactileSensation tactileSensation = new TactileSensation();
        tactileSensation.setDate(date);
        tactileSensation.setValue(value);
        AppDatabase db = AppDatabase.getInstance(context);
        new InsertTask(db).execute(tactileSensation);
    }

    public static void loadData(Context context, TactileSensationOperations.OnDataLoadedListener listener) {
        AppDatabase db = AppDatabase.getInstance(context);
        new LoadDataTask(db, listener).execute();
    }

    private static class InsertTask extends AsyncTask<TactileSensation, Void, Void> {

        private AppDatabase db;

        public InsertTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(TactileSensation... tactileSensations) {
            db.tactileSensationDao().insert(tactileSensations[0]);
            return null;
        }
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, List<TactileSensation>> {

        private TactileSensationOperations.OnDataLoadedListener listener;
        private AppDatabase db;

        public LoadDataTask(AppDatabase db, TactileSensationOperations.OnDataLoadedListener listener) {
            this.db = db;
            this.listener = listener;
        }

        @Override
        protected List<TactileSensation> doInBackground(Void... voids) {
            return db.tactileSensationDao().getAllTactileSensations();
        }

        @Override
        protected void onPostExecute(List<TactileSensation> tactileSensations) {
            super.onPostExecute(tactileSensations);
            if (listener != null) {
                listener.onDataLoaded(tactileSensations);
            }
        }
    }

    public static interface OnDataLoadedListener {
        void onDataLoaded(List<TactileSensation> tactileSensations);
    }
}
