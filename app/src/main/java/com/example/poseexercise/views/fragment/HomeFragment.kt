package com.example.poseexercise.views.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.Home
import com.example.poseexercise.R
import com.example.poseexercise.adapters.PlanAdapter
import com.example.poseexercise.adapters.RecentActivityAdapter
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.results.RecentActivityItem
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.util.MemoryManagement
import com.example.poseexercise.util.MyApplication
import com.example.poseexercise.util.MyUtils
import com.example.poseexercise.viewmodels.AddPlanViewModel
import com.example.poseexercise.viewmodels.HomeViewModel
import com.example.poseexercise.viewmodels.ResultViewModel
import com.example.poseexercise.views.activity.PlannerActivity
import com.example.poseexercise.views.activity.WorkoutActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import kotlin.math.min

class HomeFragment : Fragment(), PlanAdapter.ItemListener, MemoryManagement {
    @Suppress("PropertyName")
    val TAG = "FormFix Home Fragment"
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private var planList: List<Plan>? = emptyList()
    private var notCompletePlanList: MutableList<Plan>? = Collections.emptyList()
    private lateinit var recyclerView: RecyclerView
    private lateinit var noPlanTV: TextView
    private lateinit var goHomeButton: Button
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var adapter: PlanAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val totalRepsTextView = view.findViewById<TextView>(R.id.totalReps)
        val totalDurationTextView = view.findViewById<TextView>(R.id.totalDuration)
        // Initialize RecyclerView and its adapter for recent activity
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityRecyclerView)
        recentActivityAdapter = RecentActivityAdapter(emptyList())
        recentActivityRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        recentActivityRecyclerView.adapter = recentActivityAdapter
        // Initialize ViewModel
        resultViewModel = ResultViewModel(MyApplication.getInstance())
        lifecycleScope.launch {
            val workoutResults = resultViewModel.getRecentWorkout()
            val totalReps = workoutResults?.sumOf { it.repeatedCount } ?: 0
            totalRepsTextView.text = "Total Reps: ${totalReps.toString()}"

            val totalMinutes = workoutResults?.sumOf { it.workoutTimeInMin } ?: 0.0
            val totalDuration = formatToHHMMSS(totalMinutes)
            totalDurationTextView.text = "Total Duration: $totalDuration"
            // Call the function to load data and set up the chart
            loadDataAndSetupChart()
            // Transform WorkoutResult objects into RecentActivityItem objects
            val imageResources = arrayOf(R.drawable.blue, R.drawable.green, R.drawable.orange)
            // Transform WorkoutResult objects into RecentActivityItem objects
            val recentActivityItems = workoutResults?.mapIndexed { index, it ->
                RecentActivityItem(
                    imageResId = imageResources[index % imageResources.size],
                    exerciseType = MyUtils.exerciseNameToDisplay(it.exerciseName),
                    reps = "${it.repeatedCount} reps",
                    date = "Date: ${formatDate(it.timestamp)}",
                    duration = "Duration: ${formatToMMSS(it.workoutTimeInMin)}"
                )
            }
            // Update the adapter with the transformed data
            recentActivityAdapter.updateData(recentActivityItems ?: emptyList())
            // Check if the recentActivityItems list is empty
            if (recentActivityItems.isNullOrEmpty()) {
                recentActivityRecyclerView.isVisible = false
                // Show a message or handle the empty case as per your UI requirements
                val noActivityMessage = view.findViewById<TextView>(R.id.no_activity_message)
                noActivityMessage.text = getString(R.string.no_activities_yet)
                noActivityMessage.isVisible = true
            } else {
                recentActivityRecyclerView.isVisible = true
            }
        }
        // Initialize home view model, RecyclerView and its adapter for today's plans
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // get the list of plans from database


        // Handle back press to navigate to Home activity
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(requireActivity(), Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish() // Finish current activity
                }
            }
        )

        goHomeButton = view.findViewById(R.id.goHomeButton)

        goHomeButton.setOnClickListener {
            val intent = Intent(requireActivity(), Home::class.java)
            startActivity(intent)
        }
    }



    private fun loadDataAndSetupChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch workout results asynchronously
            workoutResults = resultViewModel.getAllResult()


        }
    }

    private fun formatToHHMMSS(totalMinutes: Double): String {
        val hours = totalMinutes.toInt() / 60
        val minutes = totalMinutes.toInt() % 60
        val seconds = ((totalMinutes - totalMinutes.toInt()) * 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun formatToMMSS(workoutTimeInMin: Double): String {
        val minutes = workoutTimeInMin.toInt()
        val seconds = ((workoutTimeInMin - minutes) * 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }



    // Return true if the timestamp is today's date
    private fun isToday(s: Long, locale: Locale = Locale.getDefault()): Boolean {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", locale)
            val netDate = Date(s)
            val currentDate = sdf.format(Date())
            sdf.format(netDate) == currentDate
        } catch (e: Exception) {
            false
        }
    }

    // Get the day from which the plan was marked as complete
    private fun getDayFromTimestamp(time: Long, locale: Locale = Locale.getDefault()): String? {
        return try {
            val sdf = SimpleDateFormat("EEEE", locale)
            val netDate = Date(time)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    // Delete the plan when user click on delete icon
    override fun onItemClicked(planId: String, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder
            .setMessage("Are you sure you want to delete the plan?")
            .setTitle("Delete plan")
            .setPositiveButton("Delete") { dialog, _ ->
                // Use AddPlanViewModel to delete the plan
                val planToDelete = notCompletePlanList?.get(position)
                planToDelete?.let {
                    val addPlanViewModel = ViewModelProvider(this)[AddPlanViewModel::class.java]
                    addPlanViewModel.deletePlan(planToDelete.id.toString())
                }

                notCompletePlanList?.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyPlan(notCompletePlanList)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Hide the recycler view if there are no plan left for today
    private fun updateEmptyPlan(plans: MutableList<Plan>?) {
        if (plans.isNullOrEmpty()) {
            noPlanTV.text = getString(R.string.there_is_no_plan_set_at_the_moment)
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun clearMemory() {
        planList = null
        notCompletePlanList = null
        workoutResults = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}
