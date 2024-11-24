package com.example.poseexercise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.data.results.GroupedWorkoutStats
import com.example.poseexercise.util.MyUtils.Companion.exerciseNameToDisplay

class WorkoutStatsAdapter(private val workoutStats: List<GroupedWorkoutStats>) :
    RecyclerView.Adapter<WorkoutStatsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseName: TextView = view.findViewById(R.id.textViewExerciseType)
        val totalReps: TextView = view.findViewById(R.id.textViewReps)
        val timesPerformed: TextView = view.findViewById(R.id.textViewPerformed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_total_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val stat = workoutStats[position]
        holder.exerciseName.text = exerciseNameToDisplay(stat.exerciseName)
        holder.totalReps.text = "Total Reps: " + stat.totalReps.toString()
        holder.timesPerformed.text = "Total Performed: " + stat.timesPerformed.toString()
    }

    override fun getItemCount() = workoutStats.size
}
