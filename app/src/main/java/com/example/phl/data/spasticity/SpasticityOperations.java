package com.example.phl.data.spasticity;

import android.content.Context;
import android.os.AsyncTask;

import com.example.phl.data.AppDatabase;

import java.util.Date;
import java.util.List;

public class SpasticityOperations {

    public static void insertData(Context context, Date date, double value) {
        Spasticity spasticity = new Spasticity();
        spasticity.setDate(date);
        spasticity.setValue(value);
        AppDatabase db = AppDatabase.getInstance(context);
        new InsertTask(db).execute(spasticity);
    }

    public static void loadData(Context context, SpasticityOperations.OnDataLoadedListener listener) {
        AppDatabase db = AppDatabase.getInstance(context);
        new LoadDataTask(db, listener).execute();
    }

    private static class InsertTask extends AsyncTask<Spasticity, Void, Void> {

        private AppDatabase db;

        public InsertTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Spasticity... spasticities) {
            db.spasticityDao().insert(spasticities[0]);
            return null;
        }
    }

    private static class LoadDataTask extends AsyncTask<Void, Void, List<Spasticity>> {

        private SpasticityOperations.OnDataLoadedListener listener;
        private AppDatabase db;

        public LoadDataTask(AppDatabase db, SpasticityOperations.OnDataLoadedListener listener) {
            this.db = db;
            this.listener = listener;
        }

        @Override
        protected List<Spasticity> doInBackground(Void... voids) {
            return db.spasticityDao().getAllSpasticities();
        }

        @Override
        protected void onPostExecute(List<Spasticity> spasticities) {
            super.onPostExecute(spasticities);
            if (listener != null) {
                listener.onDataLoaded(spasticities);
            }
        }
    }

    public interface OnDataLoadedListener {
        void onDataLoaded(List<Spasticity> spasticities);
    }
}
