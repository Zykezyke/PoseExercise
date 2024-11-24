package com.example.poseexercise

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.data.database.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ProfStatistics : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: FirebaseRepository
    private lateinit var activityTracker: Notification
    private lateinit var tvWorkouts: TextView
    private lateinit var tvReps: TextView
    private lateinit var tvForm: TextView
    private lateinit var overallActivityRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prof_statistics)
        val backButton = findViewById<ImageView>(R.id.btnBack)

        auth = FirebaseAuth.getInstance()
        activityTracker = Notification(this)

        //overall statistics workout
        tvWorkouts = findViewById(R.id.textView93)
        tvReps = findViewById(R.id.textView9)
        tvForm = findViewById(R.id.textView92)

        //workout total reps and performed
        overallActivityRecyclerView = findViewById(R.id.overallActivityRecyclerView)


        backButton.setOnClickListener{
            finish()
        }

        auth.currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("UserAccounts").child(user.uid)

            repository = FirebaseRepository(user.uid)
            updateOverallStats()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun updateOverallStats() {
        lifecycleScope.launch {
            try {
                val overallResults = repository.fetchOverallWorkoutResults()

                // Calculate total workouts
                val totalWorkouts = overallResults.size
                tvWorkouts.text = totalWorkouts.toString()

                // Calculate total reps
                val totalReps = overallResults.sumOf { it.repeatedCount }
                tvReps.text = totalReps.toString()

                // Calculate average confidence (form)
                val averageConfidence = overallResults
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


