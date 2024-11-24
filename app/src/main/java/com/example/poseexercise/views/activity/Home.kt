package com.example.poseexercise

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.poseexercise.Login
import com.example.poseexercise.R
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.views.activity.JournalActivity
import com.example.poseexercise.views.activity.PlannerActivity
import com.example.poseexercise.views.fragment.HomeFragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil

class Home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: FirebaseRepository
    private lateinit var activityTracker: Notification
    private lateinit var cardWorkout: CardView
    private lateinit var cardJournal: CardView
    private lateinit var tvWorkouts: TextView
    private lateinit var tvReps: TextView
    private lateinit var tvForm: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        activityTracker = Notification(this)

        val settingsButton = findViewById<ImageView>(R.id.btnSettings)
        val BMIButton = findViewById<CardView>(R.id.BMIButton)
        val weeklyplannerButton = findViewById<CardView>(R.id.btnWeeklyPlanner)
        val tvName = findViewById<TextView>(R.id.tvName)
        tvWorkouts = findViewById(R.id.textView9)
        tvReps = findViewById(R.id.textView10)
        tvForm = findViewById(R.id.textView11)


        WorkoutReminder(this).scheduleWorkoutCheck()

        settingsButton.setOnClickListener{
            val intent = Intent(this, com.example.poseexercise.Settings::class.java)
            startActivity(intent)
        }

        auth.currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("UserAccounts").child(user.uid)

            userRef.child("userName").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.value as? String
                    tvName.text = userName
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            repository = FirebaseRepository(user.uid)
            setupWorkoutDurationChart()
            updateWeeklyStats()
        }

        BMIButton.setOnClickListener{
            val intent = Intent(this, BmiCalculator::class.java)
            startActivity(intent)
        }

        weeklyplannerButton.setOnClickListener{
            val intent = Intent(this, WeeklyPlannerDisplay::class.java)
            startActivity(intent)
        }

        cardWorkout = findViewById(R.id.cardWorkout)

        cardWorkout.setOnClickListener {
            val intent = Intent(this, PlannerActivity::class.java)
            startActivity(intent)
        }

        cardJournal = findViewById(R.id.cardJournal)

        cardJournal.setOnClickListener {
            val intent = Intent(this, JournalActivity::class.java)
            startActivity(intent)
        }

        if (auth.currentUser == null) {
            // Redirect to Login if user is not logged in
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }



    private fun setupWorkoutDurationChart() {
        val barChart = findViewById<BarChart>(R.id.barChart)

        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.timeZone = TimeZone.getTimeZone("UTC+8")
                calendar.add(Calendar.DAY_OF_YEAR, -7) // Go back 7 days
                val startTime = calendar.timeInMillis

                val results = repository.fetchWithTimerWorkoutResults()
                    .filter { it.timestamp >= startTime }

                if (results.isEmpty()) {
                    barChart.clear()
                    barChart.setNoDataText("No workout data available for the last week.")
                    return@launch
                }

                // Group workouts by day and sum their durations
                val dailyDurations = results.groupBy { result ->
                    val date = Calendar.getInstance()
                    date.timeZone = TimeZone.getTimeZone("UTC+8")
                    date.timeInMillis = result.timestamp
                    date.get(Calendar.DAY_OF_YEAR)
                }.mapValues { (_, dailyResults) ->
                    dailyResults.sumOf { (it.workoutTimeInMin * 100) / 60 }
                }

                // Create entries for each day of the last week
                val entries = mutableListOf<BarEntry>()
                val xAxisLabels = mutableListOf<String>()
                var maxDuration = 0f

                for (i in 6 downTo 0) {
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                    val duration = dailyDurations[dayOfYear] ?: 0.0
                    maxDuration = maxOf(maxDuration, duration.toFloat())

                    entries.add(BarEntry((6 - i).toFloat(), duration.toFloat()))
                    xAxisLabels.add(
                        SimpleDateFormat(
                            "EEE",
                            Locale.getDefault()
                        ).format(calendar.time)
                    )
                }

                val customTypeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)

                val dataSet = BarDataSet(entries, "")
                dataSet.color = Color.parseColor("#3C628F")
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueTextSize = 10f
                dataSet.valueTypeface = customTypeface


                dataSet.setDrawValues(false)

                dataSet.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}min"
                    }
                }

                val barData = BarData(dataSet)
                barChart.data = barData

                // Calculate the appropriate y-axis maximum
                val yMax = if (maxDuration > 0) {
                    (ceil(maxDuration / 10f) * 10f).coerceAtLeast(10f)
                } else {
                    10f
                }

                // Customize the chart
                barChart.apply {
                    description.text = ""
                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                        typeface = customTypeface
                        textColor = Color.WHITE
                        granularity = 1f
                        labelRotationAngle = 0f
                        setDrawGridLines(false)
                    }
                    axisRight.isEnabled = false
                    axisLeft.apply {
                        axisMinimum = 0f
                        axisMaximum = yMax
                        granularity = 5f
                        valueFormatter = object : ValueFormatter() {
                            override fun getFormattedValue(value: Float): String {
                                return "${value.toInt()}min"
                            }
                        }
                        typeface = customTypeface
                        textColor = Color.WHITE
                        setDrawGridLines(true)
                    }
                    legend.isEnabled = false
                    animateY(1000)


                    setScaleEnabled(false)
                    setDoubleTapToZoomEnabled(false)

                    invalidate()
                }


                barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        if (e?.y != 0f) {
                            dataSet.setDrawValues(true)
                        } else {
                            dataSet.setDrawValues(false)
                        }
                        barChart.invalidate()
                    }

                    override fun onNothingSelected() {
                        dataSet.setDrawValues(false)
                        barChart.invalidate()
                    }
                })

            } catch (e: Exception) {
                Log.e("ChartError", "Error setting up workout duration chart", e)
                barChart.clear()
                barChart.setNoDataText("Error loading data.")
            }
        }
    }



    private fun updateWeeklyStats() {
        lifecycleScope.launch {
            try {
                val weeklyResults = repository.fetchThisWeeksWorkoutResults()

                // Calculate total workouts
                val totalWorkouts = weeklyResults.size
                tvWorkouts.text = totalWorkouts.toString()

                // Calculate total reps
                val totalReps = weeklyResults.sumOf { it.repeatedCount }
                tvReps.text = totalReps.toString()

                // Calculate average confidence (form)
                val averageConfidence = weeklyResults
                    .map { it.confidence }
                    .average()
                    .let { if (it.isNaN()) 0.0 else it }
                val formPercentage = (averageConfidence * 100).toInt()
                tvForm.text = "${formPercentage}%"

            } catch (e: Exception) {
                // Handle error case
                tvWorkouts.text = "0"
                tvReps.text = "0"
                tvForm.text = "0%"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityTracker.updateLastActiveTimestamp()
    }
}