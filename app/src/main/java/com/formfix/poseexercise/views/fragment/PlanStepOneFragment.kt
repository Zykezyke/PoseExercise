package com.formfix.poseexercise.views.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.formfix.poseexercise.Home
import com.formfix.poseexercise.R
import com.formfix.poseexercise.adapters.ExerciseAdapter
import com.formfix.poseexercise.data.database.FirebaseRepository
import com.formfix.poseexercise.data.plan.Constants
import com.formfix.poseexercise.data.plan.Plan
import com.formfix.poseexercise.util.MemoryManagement
import com.formfix.poseexercise.util.MyUtils.Companion.databaseNameToClassification
import com.formfix.poseexercise.viewmodels.AddPlanViewModel
import com.formfix.poseexercise.views.activity.Exercises
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PlanStepOneFragment : Fragment(), MemoryManagement {
    private val exerciseList = Constants.getExerciseList()
    private lateinit var btnBack: ImageView
    private var searchQuery: CharSequence? = null
    private lateinit var addPlanViewModel: AddPlanViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_step_one, container, false)
        addPlanViewModel = ViewModelProvider(this)[AddPlanViewModel::class.java]
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val activity = activity as Context
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        val levelGroup: ChipGroup = view.findViewById(R.id.chip_group)
        val workOutButton = view.findViewById<Button>(R.id.workOutBtn)
        val adapter = ExerciseAdapter(activity, findNavController(this))

        setupRecyclerView(recyclerView, adapter)
        setupBackButton(view)
        setupChipGroup(levelGroup, adapter)
        setupWorkOutButton(workOutButton)
        setupBackPressHandler()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, adapter: ExerciseAdapter) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.setExercises(exerciseList)
    }

    private fun setupBackButton(view: View) {
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun setupChipGroup(levelGroup: ChipGroup, adapter: ExerciseAdapter) {
        levelGroup.isSingleSelection = true
        levelGroup.setOnCheckedStateChangeListener { chipGroup, _ ->
            val checkedChipId = chipGroup.checkedChipId
            val chip = chipGroup.findViewById<Chip>(checkedChipId)
            if (chip != null) {
                searchQuery = chip.text
                adapter.filter.filter(searchQuery)
            } else {
                adapter.setExercises(exerciseList)
            }
        }
    }

    private fun setupWorkOutButton(workOutButton: Button) {
        workOutButton.setOnClickListener {
            lifecycleScope.launch {
                processPlanner()
            }
        }
    }




    private suspend fun processPlanner() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val calendar = Calendar.getInstance()
        calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val currentDay = when (dayOfWeek) {
            Calendar.SUNDAY -> "SundayPlanner"
            Calendar.MONDAY -> "MondayPlanner"
            Calendar.TUESDAY -> "TuesdayPlanner"
            Calendar.WEDNESDAY -> "WednesdayPlanner"
            Calendar.THURSDAY -> "ThursdayPlanner"
            Calendar.FRIDAY -> "FridayPlanner"
            Calendar.SATURDAY -> "SaturdayPlanner"
            else -> return
        }

        val currentDayName = when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> return
        }

        val databaseRef = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("UserAccounts")
            .child(userId)
            .child(currentDay)

        val repository = FirebaseRepository(userId)
        val workoutResults = repository.fetchAllWorkoutResults()

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(context, "No exercises planned for today", Toast.LENGTH_SHORT).show()
                    return
                }

                val exercisesToDo = mutableListOf<Plan>()
                for (exerciseSnapshot in snapshot.children) {
                    val exercise = exerciseSnapshot.getValue(Exercises::class.java)
                    exercise?.let {
                        val constExercise = Constants.getExerciseList().find { constEx ->
                            constEx.name == exercise.exName
                        }
                        val plannedReps = exercise.exReps?.toIntOrNull() ?: 0
                        val completedReps = workoutResults
                            .filter {
                                it.exerciseName == exercise.exName?.let { exName ->
                                    databaseNameToClassification(exName)
                                } &&
                                        isSameDay(it.timestamp, currentDayName) // Filter results for current day
                            }
                            .sumOf { it.repeatedCount }

                        if (completedReps < plannedReps) {
                            // Only add exercise to plan if not fully completed
                            val remainingReps = plannedReps - completedReps
                            val plan = Plan(
                                id = "", // Will be set by Firebase
                                exercise = exercise.exName ?: "",
                                calories = constExercise?.calorie ?: 0.0,
                                repeatCount = remainingReps,
                                selectedDays = "$currentDayName ",
                                completed = false
                            )
                            exercisesToDo.add(plan)
                            addPlanViewModel.insert(plan)
                        }
                    }
                }

                if (exercisesToDo.isEmpty()) {
                    Toast.makeText(context, "Your plan for today is already completed!", Toast.LENGTH_LONG).show()
                } else {
                    // Navigate to WorkOut fragment
                    view?.findNavController()?.navigate(R.id.action_planStepOneFragment_to_WorkOutFragment)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading exercises: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isSameDay(timestamp: Long, selectedDay: String): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
        val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
        return dayOfWeek.equals(selectedDay, ignoreCase = true)
    }



    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(requireActivity(), Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        )
    }

    override fun clearMemory() {
        searchQuery = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}