package com.formfix.poseexercise.views.fragment

import android.content.Intent
import com.formfix.poseexercise.BuildConfig
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.ai.client.generativeai.GenerativeModel
import com.formfix.poseexercise.Home
import com.formfix.poseexercise.R
import com.formfix.poseexercise.data.database.FirebaseRepository
import com.formfix.poseexercise.data.results.WorkoutResult
import com.formfix.poseexercise.views.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

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
    private lateinit var textToSpeech: TextToSpeech
    private var isTtsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initTextToSpeech()

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
                    Confidence Level: ${String.format("%.1f%%", (result.confidence * 100))}
                    """.trimIndent()
                }
                val feedback = generateWorkoutFeedback(workoutResults)
                aiFeedbackText.text = feedback
                // Speak the feedback once it's generated
                if (isTtsReady) {
                    synthesizeSpeech(feedback)
                }
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
            append("- Be no longer than 200 characters.\n")
            append("- Have a maximum of 5 sentences.\n")
            append("- Has detailed tips and advices based on the data.\n")
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

    private fun initTextToSpeech() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(1.0f)
            }
        }
    }

    private fun synthesizeSpeech(text: String) {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isTtsReady) {
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
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

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
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