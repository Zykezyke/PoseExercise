package com.example.poseexercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.views.activity.Exercises
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.util.MyUtils.Companion.databaseNameToClassification
import java.time.LocalDate
import java.time.ZoneId

class ExerciseAdapter(
        private val dataList: ArrayList<Exercises>,
        private var workoutResults: List<WorkoutResult> = emptyList()
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

                // Get the start of the day in milliseconds
                val startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1 // End of the day in milliseconds

                // Calculate completed reps for this exercise
                val completedReps = workoutResults
                        .filter { it.exerciseName == currentItem.exName?.let { it1 ->
                                databaseNameToClassification(it1)
                        } && it.timestamp in startOfDay..endOfDay }
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