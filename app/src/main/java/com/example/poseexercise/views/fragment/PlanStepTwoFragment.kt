package com.example.poseexercise.views.fragment

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.poseexercise.R
import com.example.poseexercise.Timer
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.plan.Constants
import com.example.poseexercise.util.MemoryManagement
import com.example.poseexercise.viewmodels.AddPlanViewModel
import kotlinx.coroutines.launch

class PlanStepTwoFragment : Fragment(), MemoryManagement {
    private lateinit var addPlanViewModel: AddPlanViewModel
    private var mExerciseName: String = ""
    private var mKcal: Double = 0.0
    private val selectedDays = mutableListOf<String>()
    private lateinit var days: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_step_two, container, false)
        mExerciseName = arguments?.getString("exerciseName") ?: ""
        mKcal = arguments?.getDouble("caloriesPerRep") ?: 0.0
        days = resources.getStringArray(R.array.days)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repeatEditText = view.findViewById<EditText>(R.id.repeat_count)
        val addPlanButton = view.findViewById<Button>(R.id.button)
        val exerciseImage = view.findViewById<ImageView>(R.id.exercise_image)
        val exerciseDetails = view.findViewById<TextView>(R.id.exercise_details)
        val backBtn = view.findViewById<ImageView>(R.id.backBtn)
        val timeBtn = view.findViewById<Button>(R.id.timer_button)

        // Set the default value for the exercise
        val exerciseNameText = view.findViewById<TextView>(R.id.exercise_name_header)
        exerciseNameText.text = mExerciseName

        // Find the exercise in Constants and display its details
        val exercise = Constants.getExerciseList().find { it.name == mExerciseName }
        exercise?.let {
            // Set exercise image
            it.image?.let { it1 -> exerciseImage.setImageResource(it1) }

            // Build and set exercise details text
            val detailsBuilder = StringBuilder()
            detailsBuilder.append("Level: ${it.level}\n\n")
            detailsBuilder.append("Target Muscles:\n")
            detailsBuilder.append(it.targetMuscles.joinToString(", "))
            detailsBuilder.append("\n\nHow to do it:\n")
            it.steps.forEachIndexed { index, step ->
                detailsBuilder.append("${index + 1}. $step\n")
            }
            exerciseDetails.text = detailsBuilder.toString()
        }

        // Set the min and max value for Repeat count
        setEditTextLimit(repeatEditText, 1, 100)

        // Automatically select the current day
        val calendar = java.util.Calendar.getInstance()
        val currentDay = resources.getStringArray(R.array.days)[calendar.get(java.util.Calendar.DAY_OF_WEEK) - 2]
        selectedDays.add(currentDay)


        addPlanButton.setOnClickListener {
            if (repeatEditText.text.isEmpty()) {
                showErrorMessage()
            } else {
                addPlanViewModel = ViewModelProvider(this)[AddPlanViewModel::class.java]
                val repeatCount = repeatEditText.text.toString()
                var days = ""
                for (i in selectedDays) {
                    days += "$i "
                }
                val newPlan = Plan(0.toString(), mExerciseName, mKcal, repeatCount.toInt(), days)
                lifecycleScope.launch {
                    addPlanViewModel.insert(newPlan)
                }
                view.findNavController().navigate(R.id.action_planStepTwoFragment_to_WorkOutFragment)
            }
            Log.d("PlanStepTwoFragment", "Current day: $currentDay")
        }

        backBtn.setOnClickListener {
            view.findNavController().popBackStack() // Navigates back to the previous fragment
        }

        timeBtn.setOnClickListener {
            val intent = Intent(requireContext(), Timer::class.java)

            // Pass data to the Timer activity
            intent.putExtra("exercise_name", mExerciseName)
            val exercise = Constants.getExerciseList().find { it.name == mExerciseName }
            exercise?.image?.let {
                intent.putExtra("exercise_image", it)
            }

            startActivity(intent)
        }
    }

    private fun showErrorMessage() {
        Toast.makeText(
            activity,
            "Please fill the form",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setEditTextLimit(editText: EditText, minValue: Int, maxValue: Int) {
        val inputFilter = InputFilter { source, _, _, dest, _, _ ->
            try {
                val input = (dest.toString() + source.toString()).toIntOrNull()
                if (input != null && input in minValue..maxValue) {
                    null // Input is within the range, allow the input
                } else {
                    "" // Input is outside the range, disallow the input by returning an empty string
                }
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
                "" // Return empty string for non-integer input
            }
        }
        val filters = arrayOf(inputFilter)
        editText.filters = filters
    }

    override fun clearMemory() {
        mExerciseName = ""
        mKcal = 0.0
        selectedDays.clear()
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}