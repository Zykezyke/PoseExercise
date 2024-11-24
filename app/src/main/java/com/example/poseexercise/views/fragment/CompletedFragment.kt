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

        MainActivity.workoutTimer?.let {
            workoutTimerText.text = getString(R.string.workoutResultDisplay, it)
        }
        MainActivity.workoutResultData?.let {
            CoroutineScope(Dispatchers.Main).launch {
                aiFeedbackText.text = "Generating personalized feedback..."
                val workoutResults = parseWorkoutResults(it)
                workoutResultText.text = workoutResults.joinToString("\n") { result ->
                    """
                Exercise Name: ${result.exerciseName}
                Repetitions: ${result.repeatedCount}
                Confidence Level: ${String.format("%.1f%%", (result.confidence* 100))}
                """.trimIndent()
                }
                aiFeedbackText.text = generateWorkoutFeedback(workoutResults)
            }
        } ?: run {
            workoutResultText.text = getString(R.string.noWorkoutResultDisplay)
            aiFeedbackText.text = getString(R.string.noWorkoutFeedbackAvailable)
        }

        MainActivity.apply {
            workoutResultData = null
            workoutTimer = null
        }
    }



    private suspend fun generateWorkoutFeedback(workoutResults: List<WorkoutResult>): String {
        val prompt = buildString {
            append("Provide personalized feedback for the following workout session:\n")
            workoutResults.forEach { result ->
                append("- Posture Type: ${result.exerciseName}, Repetitions: ${result.repeatedCount}, Confidence Level: ${result.confidence * 100}%\n")
            }
            append("\nThe feedback should summarize all exercises:\n")
            append("- Be no longer than 150 characters.\n")
            append("- Easy to understand and encouraging.\n")
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: fallbackWorkoutMessage(workoutResults)
        } catch (e: Exception) {
            fallbackWorkoutMessage(workoutResults)
        }
    }

    private fun fallbackWorkoutMessage(workoutResults: List<WorkoutResult>): String {
        return "Great job completing ${workoutResults.size} exercises! Keep up the steady improvement."
    }


    private fun parseWorkoutResults(resultString: String): List<WorkoutResult> {
        println("Parsing workout results: $resultString")
        val workoutResults = mutableListOf<WorkoutResult>()

        resultString.lines().forEach { line ->
            val parts = line.split(",")
            if (parts.size >= 3) {
                try {
                    val exerciseName = parts[0].trim()
                    val repeatedCount = parts[1].trim().toInt()
                    val confidence = parts[2].trim().toFloat()

                    workoutResults.add(
                        WorkoutResult(
                            exerciseName = exerciseName,
                            repeatedCount = repeatedCount,
                            confidence = confidence,
                            timestamp = System.currentTimeMillis(),
                            calorie = 0.0,
                            workoutTimeInMin = 0.0
                        )
                    )
                } catch (e: Exception) {
                    println("Error parsing line: $line - ${e.message}")
                }
            } else {
                println("Invalid format for line: $line")
            }
        }
        return workoutResults
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

    override fun onDestroy() {
        super.onDestroy()
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