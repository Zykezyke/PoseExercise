package com.formfix.poseexercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.formfix.poseexercise.views.activity.Exercises
import com.formfix.poseexercise.data.results.WorkoutResult
import com.formfix.poseexercise.util.MyUtils.Companion.databaseNameToClassification
import java.util.Calendar
import java.util.Locale

class ExerciseAdapter(
        private val dataList: ArrayList<Exercises>,
        private var workoutResults: List<WorkoutResult> = emptyList(),
        private val selectedDay: String
) : RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

        private lateinit var cListener: onItemClickListener

        interface onItemClickListener {
                fun onItemClick(position: Int)
        }

        fun setOnItemClickListener(clickListener: onItemClickListener) {
                cListener = clickListener
        }

        // Function to update workout results
        fun updateWorkoutResults(newResults: List<WorkoutResult>) {
                workoutResults = newResults
                notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_exercises, parent, false)
                return ViewHolder(itemView, cListener)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val currentItem = dataList[position]
                holder.rvExName.text = currentItem.exName

                // Get the planned reps from Exercises
                val plannedReps = currentItem.exReps?.toIntOrNull() ?: 0

                // Calculate completed reps for this exercise and selected day
                val completedReps = workoutResults
                        .filter {
                                // Match exercise name with classification
                                it.exerciseName == currentItem.exName?.let { exName ->
                                        databaseNameToClassification(exName)
                                } &&
                                        // Filter only results for the selected planner day
                                        isSameDay(it.timestamp, selectedDay)
                        }
                        .sumOf { it.repeatedCount }

                // Update the reps text to show progress
                holder.rvExReps.text = "Reps: $completedReps/$plannedReps"

                // Calculate progress percentage
                val progress = if (plannedReps > 0) {
                        (completedReps.toFloat() / plannedReps.toFloat() * 100).toInt()
                } else {
                        0
                }

                // Update progress bar
                holder.progressBar.progress = progress
                holder.progressText.text = "$progress%"
        }


        private fun isSameDay(timestamp: Long, selectedDay: String): Boolean {
                val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
                val dayOfWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                return dayOfWeek.equals(selectedDay, ignoreCase = true)
        }

        override fun getItemCount(): Int {
                return dataList.size
        }

        class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
                val rvExName: TextView = itemView.findViewById(R.id.txtViewExName)
                val rvExReps: TextView = itemView.findViewById(R.id.txtRepsC)
                val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
                val progressText: TextView = itemView.findViewById(R.id.progressText)

                init {
                        itemView.setOnClickListener {
                                clickListener.onItemClick(adapterPosition)
                        }
                }
        }
}