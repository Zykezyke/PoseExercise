package com.formfix.poseexercise

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.formfix.poseexercise.adapters.WorkoutStatsAdapter
import com.formfix.poseexercise.data.database.FirebaseRepository
import com.formfix.poseexercise.data.results.GroupedWorkoutStats
import com.formfix.poseexercise.util.MyUtils.Companion.exerciseNameToDisplay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class ProfStatistics : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var repository: FirebaseRepository
    private lateinit var activityTracker: Notification
    private lateinit var tvWorkouts: TextView
    private lateinit var tvReps: TextView
    private lateinit var tvForm: TextView
    private lateinit var overallActivityRecyclerView: RecyclerView
    private lateinit var favoriteWorkoutText: TextView
    private lateinit var favoriteWorkoutImage: ImageView


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
        favoriteWorkoutText = findViewById(R.id.textView23)
        favoriteWorkoutImage = findViewById(R.id.imageView3)

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
            setupWorkoutStatsList()
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
                val allResult = repository.fetchAllWorkoutResults()

                // Calculate total workouts
                val totalWorkouts = allResult.size
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

    private fun setupWorkoutStatsList() {
        lifecycleScope.launch {
            try {
                val workoutResults = repository.fetchAllWorkoutResults()

                // Group results by exercise name
                val groupedStats = workoutResults
                    .groupBy { it.exerciseName }
                    .map { (name, results) ->
                        GroupedWorkoutStats(
                            exerciseName = name,
                            totalReps = results.sumOf { it.repeatedCount },
                            timesPerformed = results.size
                        )
                    }
                    .sortedByDescending { it.timesPerformed }

                // Set up RecyclerView
                val adapter = WorkoutStatsAdapter(groupedStats)
                overallActivityRecyclerView.adapter = adapter
                overallActivityRecyclerView.layoutManager = LinearLayoutManager(this@ProfStatistics)

                // Update favorite workout
                groupedStats.maxByOrNull { it.timesPerformed }?.let { mostCommon ->
                    favoriteWorkoutText.text = exerciseNameToDisplay(mostCommon.exerciseName)
                    // Set the appropriate image based on the exercise name
                    val imageResource = when(exerciseNameToDisplay(mostCommon.exerciseName)) {
                        "Push up" -> R.drawable.push_up
                        "Lunge" -> R.drawable.reverse_lunges
                        "Squat" -> R.drawable.squat
                        "Sit up" -> R.drawable.sit_ups
                        "Jumping jacks" -> R.drawable.jumping_jacks
                        else -> R.drawable.pushup // default image
                    }
                    favoriteWorkoutImage.setImageResource(imageResource)
                }

            } catch (e: Exception) {
                // Handle error case
                Toast.makeText(this@ProfStatistics, "Error loading workout stats", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityTracker.updateLastActiveTimestamp()
    }
}


