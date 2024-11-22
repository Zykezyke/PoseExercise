package com.example.poseexercise

import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import org.w3c.dom.Text

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