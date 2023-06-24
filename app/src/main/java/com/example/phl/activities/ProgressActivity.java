package com.example.phl.activities;

import android.os.Bundle;

import com.example.phl.R;

import android.widget.Toast;

import com.example.phl.data.ProgressData;
import com.example.phl.data.sensation.TactileSensation;
import com.example.phl.data.sensation.TactileSensationOperations;
import com.example.phl.data.spasticity.Spasticity;
import com.example.phl.data.spasticity.SpasticityOperations;
import com.example.phl.services.RemoteControlService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProgressActivity extends MyBaseActivity {

    private LineChart lineChart;

    private static final int TACTILE_SENSATION_TAB_INDEX = 0;
    private static final int SPASTICITY_TAB_INDEX = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        lineChart = findViewById(R.id.lineChart);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        loadTactileSensationData();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == TACTILE_SENSATION_TAB_INDEX) {
                    loadTactileSensationData();
                } else if (position == SPASTICITY_TAB_INDEX) {
                    loadSpasticityData();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselection
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection
            }
        });
        registerCommand("Exit", new RemoteControlService.CommandHandler() {
            @Override
            public void handle() {
                finish();
            }
        });
    }

    private void loadTactileSensationData() {
        TactileSensationOperations.loadData(this, new TactileSensationOperations.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(List<TactileSensation> tactileSensations) {
                if (tactileSensations != null && tactileSensations.size() > 0) {
                    configureLineChart(tactileSensations);
                } else {
                    ProgressData test = new TactileSensation();
                    Toast.makeText(ProgressActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSpasticityData() {
        SpasticityOperations.loadData(this, new SpasticityOperations.OnDataLoadedListener() {
            @Override
            public void onDataLoaded(List<Spasticity> spasticities ) {
                if (spasticities != null && spasticities.size() > 0) {
                    configureLineChart(spasticities);
                } else {
                    Toast.makeText(ProgressActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void configureLineChart(List<? extends ProgressData> data) {
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            ProgressData datapoint = data.get(i);
            entries.add(new Entry(i, datapoint.getValue().floatValue()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Label");
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter(data));
        xAxis.setLabelCount(data.size(), true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Set the minimum range of the x-axis to at least 2 integers
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum( data.size()-1);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        // Enable zooming
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);

        lineChart.animateX(100);

        lineChart.invalidate();
    }

    private static class DateValueFormatter extends ValueFormatter {
        private final SimpleDateFormat dateFormat;
        List<? extends ProgressData> data;

        public DateValueFormatter(List<? extends ProgressData> data) {
            dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            this.data = data;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < data.size()) {
                return dateFormat.format(data.get(index).getDate());
            }
            return "";
        }
    }
}