package com.example.phl.activities

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.phl.R
import com.example.phl.data.AbstractData
import com.example.phl.data.AbstractResultData
import com.example.phl.data.AppDatabase
import com.example.phl.data.sensation.TactileSensation
import com.example.phl.data.spasticity.Spasticity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class ProgressActivity : MyBaseActivity() {
    private var lineChart: LineChart? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)
        lineChart = findViewById(R.id.lineChart)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        loadTactileSensationData()
        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (position == TACTILE_SENSATION_TAB_INDEX) {
                    loadTactileSensationData()
                } else if (position == SPASTICITY_TAB_INDEX) {
                    loadSpasticityData()
                } else if (position == BALL_TEST_TAB_INDEX) {
                    loadBallTestData()
                } else if (position == TILT_TEST_TAB_INDEX) {
                    loadTiltTestData()
                } else if (position == MAS_TEST_TAB_INDEX) {
                    // TODO: load MAS test data
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Handle tab unselection
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Handle tab reselection
            }
        })
    }

    private fun loadTactileSensationData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val tactileSensations = db.tactileSensationDao().getAll()

            withContext(Dispatchers.Main) {
                if (tactileSensations.isNotEmpty()) {
                    configureLineChart(tactileSensations)
                } else {

                    Toast.makeText(this@ProgressActivity, "No data found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadSpasticityData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val spasticities = db.spasticityDao().getAll()
            withContext(Dispatchers.Main) {
                if (spasticities.isNotEmpty()) {
                    configureLineChart(spasticities)
                } else {
                    Toast.makeText(this@ProgressActivity, "No data found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadBallTestData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val ballTests = db.ballTestResultDao().getAll()
            withContext(Dispatchers.Main) {
                if (ballTests.isNotEmpty()) {
                    configureLineChart(ballTests)
                } else {
                    Toast.makeText(this@ProgressActivity, "No data found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun loadTiltTestData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            val tiltTests = db.tiltTestResultDao().getAll()
            withContext(Dispatchers.Main) {
                if (tiltTests.isNotEmpty()) {
                    configureLineChart(tiltTests)
                } else {
                    Toast.makeText(this@ProgressActivity, "No data found", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun configureLineChart(data: List<AbstractResultData>) {
        val entries: MutableList<Entry> = ArrayList()
        for (i in data.indices) {
            val datapoint = data[i]
            entries.add(Entry(i.toFloat(), datapoint.score.toFloat()))
        }
        val dataSet = LineDataSet(entries, "Label")
        val lineData = LineData(dataSet)
        lineChart!!.data = lineData
        val xAxis = lineChart!!.xAxis
        xAxis.valueFormatter = DateValueFormatter(data)
        xAxis.setLabelCount(data.size, true)
        xAxis.position = XAxis.XAxisPosition.BOTTOM

        // Set the minimum range of the x-axis to at least 2 integers
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = (data.size - 1).toFloat()
        lineChart!!.axisRight.isEnabled = false
        lineChart!!.description.isEnabled = false
        lineChart!!.legend.isEnabled = false

        // Enable zooming
        lineChart!!.setPinchZoom(true)
        lineChart!!.isDoubleTapToZoomEnabled = true
        lineChart!!.animateX(100)
        lineChart!!.invalidate()
    }

    private class DateValueFormatter(data: List<AbstractData>) : ValueFormatter() {
        private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd")
        var data: List<AbstractData>

        init {
            this.data = data
        }

        override fun getFormattedValue(value: Float): String {
            val index = value.toInt()
            return if (index >= 0 && index < data.size) {
                data[index].time.format(dateFormat)
            } else ""
        }
    }

    companion object {
        private const val TACTILE_SENSATION_TAB_INDEX = 0
        private const val SPASTICITY_TAB_INDEX = 1
        private const val BALL_TEST_TAB_INDEX = 2
        private const val TILT_TEST_TAB_INDEX = 3
        private const val MAS_TEST_TAB_INDEX = 4
    }
}