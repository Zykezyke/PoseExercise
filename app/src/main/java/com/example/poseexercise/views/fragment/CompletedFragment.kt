package com.example.poseexercise.views.fragment

import android.content.Intent
import com.example.poseexercise.BuildConfig
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.ai.client.generativeai.GenerativeModel
import com.example.poseexercise.Home
import com.example.poseexercise.R
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.views.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CompletedFragment: A fragment displayed when a workout is successfully completed.
 *
 * This fragment provides a button to navigate back to the home screen and displays the workout
 * result and timer information.
 */
class CompletedFragment : Fragment() {

    private lateinit var navigateToHomeButton: Button
    private lateinit var generativeModel: GenerativeModel
    private lateinit var aiFeedbackText: TextView
    private lateinit var repository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the generative AI model
        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
        repository = FirebaseRepository(userId)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_completed, container, false)

        navigateToHomeButton = view.findViewById(R.id.goToHomeFromComplete)
        aiFeedbackText = view.findViewById(R.id.textView5)

        navigateToHomeButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repository.deleteAllPlans()
                    println("All plans deleted successfully")
                } catch (e: Exception) {
                    println("Failed to delete plans: $e")
                } finally {
                    navigateToHome()
                }
            }
        }

        // Handle back press to navigate to Home activity
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repository.deleteAllPlans()
                            println("All plans deleted successfully")
                        } catch (e: Exception) {
                            println("Failed to delete plans: $e")
                        } finally {
                            navigateToHome()
                        }
                    }
                }
            }
        )

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workoutResultText: TextView = view.findViewById(R.id.workoutResult_textView)
        val workoutTimerText: TextView = view.findViewById(R.id.workoutTimer_textView)

        // Display workout results and timer
        MainActivity.workoutTimer?.let {
            workoutTimerText.text = getString(R.string.workoutResultDisplay, it)
        }
        MainActivity.workoutResultData?.let {
            workoutResultText.text = getString(R.string.workoutResultDisplay, it)

            // Generate AI feedback based on workout results
            CoroutineScope(Dispatchers.Main).launch {
                aiFeedbackText.text = "Generating personalized feedback..."
                val workoutResult = parseWorkoutResult(it)
                workoutResultText.text = """
                    Exercise Name: ${workoutResult.exerciseName}
                    Repetitions: ${workoutResult.repeatedCount}
                    Confidence Level: ${String.format("%.1f%%", workoutResult.confidence)}
                """.trimIndent()
                val feedback = generateWorkoutFeedback(workoutResult)
                aiFeedbackText.text = feedback
            }
        } ?: run {
            workoutResultText.text = getString(R.string.noWorkoutResultDisplay)
            aiFeedbackText.text = getString(R.string.noWorkoutFeedbackAvailable)
        }

        // Reset workout data in the MainActivity
        MainActivity.apply {
            workoutResultData = null
            workoutTimer = null
        }
    }


    private suspend fun generateWorkoutFeedback(workoutResult: WorkoutResult): String {
        val prompt = """
            Provide personalized feedback for the following workout session:
            - Posture Type: ${workoutResult.exerciseName}
            - Repetitions: ${workoutResult.repeatedCount}
            - Confidence Level: ${workoutResult.confidence}%
            
            The sentence should be:
        - No longer than 100 characters.
        - Easy to understand with simple vocabulary.
        - Encouraging and actionable.
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: fallbackWorkoutMessage(workoutResult)
        } catch (e: Exception) {
            fallbackWorkoutMessage(workoutResult)
        }
    }

    private fun fallbackWorkoutMessage(workoutResult: WorkoutResult): String {
        return "Great job completing your workout! With a confidence level of ${workoutResult.confidence}%, you're improving steadily. Keep up the good work!"
    }

    private fun parseWorkoutResult(resultString: String): WorkoutResult {
        println("Parsing workout result: $resultString")

        // Check if the result is in the expected comma-separated format
        if (resultString.contains(",")) {
            val data = resultString.split(",")
            return WorkoutResult(
                exerciseName = data[0],
                repeatedCount = data[1].toInt(),
                confidence = data[2].toFloat(),
                timestamp = System.currentTimeMillis(),
                calorie = 0.0,
                workoutTimeInMin = 0.0
            )
        } else {
            // If the string doesn't contain comma, parse it differently
            // Assume the format is "Exercise Name: Repetitions"
            val parts = resultString.split(":")
            val exerciseName = parts[0].trim()
            val repeatedCount = parts[1].trim().toIntOrNull() ?: 0

            // Use default values for other fields
            return WorkoutResult(
                exerciseName = exerciseName,
                repeatedCount = repeatedCount,
                confidence = 0.0f,
                timestamp = System.currentTimeMillis(),
                calorie = 0.0,
                workoutTimeInMin = 0.0
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAllPlans()
                println("All plans deleted successfully")
            } catch (e: Exception) {
                println("Failed to delete plans: $e")
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }

}