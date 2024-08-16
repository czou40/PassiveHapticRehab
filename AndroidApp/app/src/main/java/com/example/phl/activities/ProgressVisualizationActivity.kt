package com.example.phl.activities

import android.os.Bundle
import android.text.Layout
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phl.activities.ui.theme.PHLTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter

import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.phl.R
import com.example.phl.activities.unity.getCurrentActivity
import com.example.phl.data.AppDatabase
import com.example.phl.data.unity.IUnityGameResult
import com.example.phl.data.unity.ShoulderExtensionFlexionResult
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.decoration.rememberHorizontalLine
import com.patrykandpatrick.vico.compose.cartesian.fullWidth

import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberLayeredComponent
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShadow
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.compose.common.shape.markerCornered
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalDimensions
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Insets
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel

import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.Corner
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.Instant
import kotlin.math.min
import kotlin.math.round
import kotlin.random.Random


class ProgressVisualizationActivity : ComponentActivity() {
    private val bottomAxisValueFormatter =
        CartesianValueFormatter { x, _, _ -> dateFromDaysSinceEpoch(x.toLong(), true) }
//    private var shoulderExtensionFlexionResults: List<ShoulderExtensionFlexionResult>? = null
    private var shoulderExtensionFlexionResults by mutableStateOf<List<ShoulderExtensionFlexionResult>?>(null)

    companion object {
        enum class DisplayRange {
            ALL,
            WEEK,
            MONTH,
            YEAR
        }

        enum class DisplayScore {
            DEFAULT_SCORE,
            MINIMIZING_SCORE,
            MAXIMIZING_SCORE
        }

        private const val LABEL_BACKGROUND_SHADOW_RADIUS_DP = 4f
        private const val LABEL_BACKGROUND_SHADOW_DY_DP = 2f
        private const val CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER = 1.4f

        private const val BAR_COLOR = 0xFF81DC5F
        private const val HORIZONTAL_LINE_COLOR = 0XFF20A086
        private const val HORIZONTAL_LINE_THICKNESS_DP = 2f
        private const val HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP = 8f
        private const val HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP = 2f
        private const val HORIZONTAL_LINE_LABEL_MARGIN_DP = 4f

        private fun daysSinceEpoch(date: String): Long {
            val epochDate = LocalDate.of(1970, 1, 1)
            val givenDate = LocalDate.parse(date)
            return ChronoUnit.DAYS.between(epochDate, givenDate)
        }

        private fun daysSinceEpoch(timestamp: Long): Long {
            val epochDate = LocalDate.of(1970, 1, 1)
            val localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            return ChronoUnit.DAYS.between(epochDate, localDate)
        }

        private fun daysSinceEpoch(): Long {
            val epochDate = LocalDate.of(1970, 1, 1)
            val today = LocalDate.now()
            return ChronoUnit.DAYS.between(epochDate, today)
        }

        private fun dateFromDaysSinceEpoch(days: Long, monthAndDayOnly: Boolean): String {
            val epochDate = LocalDate.of(1970, 1, 1)
            val targetDate = epochDate.plusDays(days)
            if (!monthAndDayOnly) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                return targetDate.format(formatter)
            } else {
                val formatter = DateTimeFormatter.ofPattern("MMM d")
                return targetDate.format(formatter)
            }
        }

        private fun dateToday(): String {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            return today.format(formatter)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            val result = AppDatabase.getInstance(applicationContext).shoulderExtensionFlexionResultDao().getAll()
            withContext(Dispatchers.Main) {
                shoulderExtensionFlexionResults = result
                Log.d("Results", shoulderExtensionFlexionResults.toString())
                Log.d("Results", shoulderExtensionFlexionResults?.size.toString())
            }
        }
        setContent {
            ProgressVisualizationActivityLayout()
        }
    }

    @Composable
    fun ProgressVisualizationList(cards: List<ProgressVisualizationCardViewModel>) {
        var displayRange by remember { mutableStateOf(DisplayRange.MONTH) }
        val activity = getCurrentActivity()
        Log.d("Display Type", displayRange.toString())
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF20A086))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            activity?.finish()
                        },
                    tint = Color.White,
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "My Progress",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            )
            {
                Button(onClick = {
                    displayRange = DisplayRange.ALL
                }) {
                    Text("All")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    displayRange = DisplayRange.WEEK
                }) {
                    Text("Past Week")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    displayRange = DisplayRange.MONTH
                }) {
                    Text("Past Month")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    displayRange = DisplayRange.YEAR
                }) {
                    Text("Past Year")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    shoulderExtensionFlexionResults = generateData()
                }) {
                    Text("Dummy Data")
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                content = {
                    items(cards) { card ->
                        card.displayRange = displayRange
                        Column(
                            modifier = Modifier
                                .padding(12.dp) // Add padding between items
                        ) {
                            ProgressVisualizationCard(card)
                        }
                    }
                },
            )
        }
    }

    data class ProgressVisualizationCardViewModel(
        val gameName: String,
        val gameDescription: String,
        val gameIcon: Int,
        val scoreName: String,
        var data: List<IUnityGameResult>?,
        var displayRange: DisplayRange = DisplayRange.MONTH,
        var displayScore: DisplayScore = DisplayScore.DEFAULT_SCORE
    )

    @Composable
    fun ProgressVisualizationCard(
        viewModel: ProgressVisualizationCardViewModel
    ) {
        val context = LocalContext.current // Get the local context to use for the Intent
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = viewModel.gameIcon),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(72.dp)
                            .width(72.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column() {
                        Text(
                        text = viewModel.gameName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )
                        Text(
                            text = viewModel.gameDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.scoreName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )
                VicoChart(data = viewModel.data, displayRange = viewModel.displayRange, displayScore = viewModel.displayScore)
            }
        }
    }

    private fun filterLastOccurrence(data: List<IUnityGameResult>): List<IUnityGameResult> {
        if (data.isEmpty()) return emptyList()

        val filteredList = mutableListOf<IUnityGameResult>()
        var lastDate = -1L;

        for (result in data) {
            val currDate = daysSinceEpoch(result.startTime)
            if (currDate !=lastDate) {
                filteredList.add(result)
                lastDate = currDate
            } else {
                filteredList[filteredList.size - 1] = result
            }

        }
        return filteredList
    }

    private suspend fun updateChart(
        modelProducer: CartesianChartModelProducer,
        data: List<IUnityGameResult>,
        displayScore: DisplayScore = DisplayScore.DEFAULT_SCORE
    ) {
        val filtered = filterLastOccurrence(data)
        val processedDates = filtered.map { daysSinceEpoch(it.startTime) }
        val processedScores = filtered.map {
            when (displayScore) {
                DisplayScore.DEFAULT_SCORE -> it.score
                DisplayScore.MINIMIZING_SCORE -> it.minimizingScore
                DisplayScore.MAXIMIZING_SCORE -> it.maximizingScore
            }
        }
        modelProducer.runTransaction {
            columnSeries {
                series(
                    x = processedDates,
                    y = processedScores,
                )
            }
        }
    }

    @Composable
    fun VicoChart(data: List<IUnityGameResult>?, displayRange: DisplayRange = DisplayRange.MONTH, displayScore: DisplayScore = DisplayScore.DEFAULT_SCORE) {
        val modelProducer = remember { CartesianChartModelProducer() }
        val lastScore = when (displayScore) {
            DisplayScore.DEFAULT_SCORE -> data?.lastOrNull()?.score ?: -1.0
            DisplayScore.MINIMIZING_SCORE -> data?.lastOrNull()?.minimizingScore ?: -1.0
            DisplayScore.MAXIMIZING_SCORE -> data?.lastOrNull()?.maximizingScore ?: -1.0
        }
        val secondToLastScore = when (displayScore) {
            DisplayScore.DEFAULT_SCORE -> data?.getOrNull(data.size - 2)?.score
            DisplayScore.MINIMIZING_SCORE -> data?.getOrNull(data.size - 2)?.minimizingScore
            DisplayScore.MAXIMIZING_SCORE -> data?.getOrNull(data.size - 2)?.maximizingScore
        }
        val percentageChange = secondToLastScore?.let { 100*((lastScore - it) / it)}
        val startDate = daysSinceEpoch(data?.firstOrNull()?.startTime ?: daysSinceEpoch())
        val currentDate = daysSinceEpoch()
        val initialScroll = when (displayRange) {
            DisplayRange.ALL -> Scroll.Absolute.Start
            DisplayRange.WEEK -> Scroll.Absolute.x(currentDate - 7 + 1.0)
            DisplayRange.MONTH -> Scroll.Absolute.x(currentDate - 30 + 1.0)
            DisplayRange.YEAR -> Scroll.Absolute.x(currentDate - 365 + 1.0)
        }
        val initialZoom = when (displayRange) {
            DisplayRange.ALL -> Zoom.Content
            DisplayRange.WEEK -> Zoom.x(7.0)
            DisplayRange.MONTH -> Zoom.x(30.0)
            DisplayRange.YEAR -> Zoom.x(365.0)
        }
        var chartReady by remember { mutableStateOf(false) }
        val scrollState = rememberVicoScrollState(
            initialScroll = initialScroll
        )

        val zoomState = rememberVicoZoomState(
            initialZoom = initialZoom,
        )

        LaunchedEffect(data) {
            // Make the placeholder disappear when the chart is ready
            // The chart's min x should be the min of the processed dates, and max should be the current date
            if (!data.isNullOrEmpty()) {
                updateChart(modelProducer, data, displayScore)
                chartReady = true
            } else {
                chartReady = false
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            if (chartReady) {
                // Actual chart that will overlap the placeholder
                CartesianChartHost(
                    rememberCartesianChart(
                        rememberColumnCartesianLayer(
                            ColumnCartesianLayer.ColumnProvider.series(
                                rememberLineComponent(
                                    color = Color(BAR_COLOR),
                                    thickness = 48.dp,
                                    shape = remember { Shape.Rectangle }
                                )
                            ),
                            axisValueOverrider = AxisValueOverrider.fixed(minX = min(startDate.toDouble(),currentDate.toDouble()-29), maxX = currentDate.toDouble())
                        ),
                        startAxis = rememberStartAxis(
                            valueFormatter = { x, _, _ -> "${x.toInt()}°"}
                        ),
                        bottomAxis = rememberBottomAxis(
                            valueFormatter = bottomAxisValueFormatter,
                            itemPlacer = remember {
                                HorizontalAxis.ItemPlacer.default(
                                    spacing = 1,
                                    addExtremeLabelPadding = true,
                                )
                            }
                        ),
                        marker = rememberMarker(showIndicator = false),
                        horizontalLayout = HorizontalLayout.fullWidth(),
                        decorations = listOf(rememberComposeHorizontalLine(lastScore, percentageChange)),
                    ),
                    modelProducer,
                    modifier = Modifier.fillMaxSize(),
                    scrollState = scrollState,
                    zoomState = zoomState,
                )
            } else {
                // Placeholder for the chart, showing a gray block with the text "Chart goes here"
                // This is because CartesianChartHost cannot be shown in the preview.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = "No Data Available",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }


    @Composable
    private fun rememberComposeHorizontalLine(y: Double, percentageChange:Double?=null): HorizontalLine {
        val color = Color(HORIZONTAL_LINE_COLOR)
        return rememberHorizontalLine(
            y = { y },
            line = rememberLineComponent(
                color,
                HORIZONTAL_LINE_THICKNESS_DP.dp,
                shape = Shape.dashed(Shape.Rectangle, 10.dp, 5.dp)
            ),
            labelComponent =
            rememberTextComponent(
                margins = Dimensions.of(HORIZONTAL_LINE_LABEL_MARGIN_DP.dp),
                padding =
                Dimensions.of(
                    HORIZONTAL_LINE_LABEL_HORIZONTAL_PADDING_DP.dp,
                    HORIZONTAL_LINE_LABEL_VERTICAL_PADDING_DP.dp,
                ),
                background = rememberShapeComponent(color, Shape.Pill),
                color = Color.White
            ),
            label =  {
                if (percentageChange != null) {
                    val change = round(percentageChange).toInt()
                    val changeString = if (change >= 0) "↑$change" else "↓${-change}"
                    String.format("%.2f° $changeString%%", y)
                } else {
                    String.format("%.2f°", y)
                }
            }
        )
    }

    @Preview(
        showBackground = true,
        name = "10-inch Tablet Landscape",
        widthDp = 1200,
        heightDp = 750
    )
    @Composable
    fun ProgressVisualizationActivityLayout() {
        val cards = listOf(
            ProgressVisualizationCardViewModel(
                gameName = "Shoulder Flexion",
                gameDescription = "Ability to lift up the arm",
                gameIcon =  R.drawable.shoulder_flexion_icon_small,
                scoreName = "Range of Motion",
                data = shoulderExtensionFlexionResults,
                displayScore = DisplayScore.MAXIMIZING_SCORE
            ),
            ProgressVisualizationCardViewModel(
                gameName = "Shoulder Extension",
                gameDescription = "Ability to push back the arm",
                gameIcon = R.drawable.shoulder_extension_icon_small,
                scoreName = "Range of Motion",
                data = shoulderExtensionFlexionResults,
                displayScore = DisplayScore.MINIMIZING_SCORE
            )
        )

        PHLTheme {
            ProgressVisualizationList(cards)
        }
    }

    @Composable
    internal fun rememberMarker(
        labelPosition: DefaultCartesianMarker.LabelPosition = DefaultCartesianMarker.LabelPosition.Top,
        showIndicator: Boolean = true,
    ): CartesianMarker {
        val labelBackgroundShape = Shape.markerCornered(Corner.FullyRounded)
        val labelBackground =
            rememberShapeComponent(
                color = MaterialTheme.colorScheme.surfaceBright,
                shape = labelBackgroundShape,
                shadow =
                rememberShadow(
                    radius = LABEL_BACKGROUND_SHADOW_RADIUS_DP.dp,
                    dy = LABEL_BACKGROUND_SHADOW_DY_DP.dp,
                ),
            )
        val label =
            rememberTextComponent(
                color = Color.Black,
                textAlignment = Layout.Alignment.ALIGN_CENTER,
                padding = Dimensions.of(8.dp, 4.dp),
                background = labelBackground,
                minWidth = TextComponent.MinWidth.fixed(40.dp),
            )
        val indicatorFrontComponent =
            rememberShapeComponent(MaterialTheme.colorScheme.surface, Shape.Pill)
        val indicatorCenterComponent = rememberShapeComponent(shape = Shape.Pill)
        val indicatorRearComponent = rememberShapeComponent(shape = Shape.Pill)
        val indicator =
            rememberLayeredComponent(
                rear = indicatorRearComponent,
                front =
                rememberLayeredComponent(
                    rear = indicatorCenterComponent,
                    front = indicatorFrontComponent,
                    padding = Dimensions.of(5.dp),
                ),
                padding = Dimensions.of(10.dp),
            )
        val guideline = rememberAxisGuidelineComponent()
        return remember(label, labelPosition, indicator, showIndicator, guideline) {
            object :
                DefaultCartesianMarker(
                    label = label,
                    labelPosition = labelPosition,
                    indicator =
                    if (showIndicator) {
                        { color ->
                            LayeredComponent(
                                rear = ShapeComponent(color, Shape.Pill),
                                front =
                                LayeredComponent(
                                    rear =
                                    ShapeComponent(
                                        color = color,
                                        shape = Shape.Pill,
                                        shadow = Shadow(radiusDp = 12f, color = color),
                                    ),
                                    front = indicatorFrontComponent,
                                    padding = Dimensions.of(5.dp),
                                ),
                                padding = Dimensions.of(10.dp),
                            )
                        }
                    } else {
                        null
                    },
                    indicatorSizeDp = 36f,
                    guideline = guideline,
                    valueFormatter = DefaultCartesianMarkerValueFormatter(colorCode = false),
                ) {
                override fun updateInsets(
                    context: CartesianMeasureContext,
                    horizontalDimensions: HorizontalDimensions,
                    model: CartesianChartModel,
                    insets: Insets,
                ) {
                    with(context) {
                        val baseShadowInsetDp =
                            CLIPPING_FREE_SHADOW_RADIUS_MULTIPLIER * LABEL_BACKGROUND_SHADOW_RADIUS_DP
                        var topInset = (baseShadowInsetDp - LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                        var bottomInset = (baseShadowInsetDp + LABEL_BACKGROUND_SHADOW_DY_DP).pixels
                        when (labelPosition) {
                            LabelPosition.Top,
                            LabelPosition.AbovePoint -> topInset += label.getHeight(context) + tickSizeDp.pixels

                            LabelPosition.Bottom -> bottomInset += label.getHeight(context) + tickSizeDp.pixels
                            LabelPosition.AroundPoint -> {}
                        }
                        insets.ensureValuesAtLeast(top = topInset, bottom = bottomInset)
                    }
                }
            }
        }
    }

    fun generateData(): List<ShoulderExtensionFlexionResult> {
        val startDate = LocalDate.of(2023, 1, 1)
        val endDate = LocalDate.of(2024, 8, 4)

        // Utility function to generate random dates within a range
        fun generateRandomDate(start: LocalDate, end: LocalDate): Long {
            val startEpochDay = start.toEpochDay()
            val endEpochDay = end.toEpochDay()
            val randomEpochDay = Random.nextLong(startEpochDay, endEpochDay)
            return LocalDate.ofEpochDay(randomEpochDay).atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        // Function to generate random angles
        fun generateRandomAngles(size: Int, minAngle: Double, maxAngle: Double): List<Double> {
            return List(size) { Random.nextDouble(minAngle, maxAngle) }
        }

        val result = List(100) {
            val minAngles = generateRandomAngles(10, 0.0, 90.0) // Example range for minimum angles
            val maxAngles =
                generateRandomAngles(10, 90.0, 180.0) // Example range for maximum angles
            val startTime = generateRandomDate(startDate, endDate)
            val endTime = generateRandomDate(startDate, endDate)

            ShoulderExtensionFlexionResult(
                minAngles = minAngles,
                maxAngles = maxAngles,
                startTime = startTime,
                endTime = endTime
            )
        }

        // Sort the results by start time
        return result.sortedBy { it.startTime }
    }
}
