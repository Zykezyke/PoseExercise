package com.example.poseexercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.views.activity.Exercises

class ExerciseAdapter (private val dataList: ArrayList<Exercises>) :
        RecyclerView.Adapter<ExerciseAdapter.ViewHolder>() {

        private lateinit var cListener : onItemClickListener
        interface onItemClickListener{
                fun onItemClick(position: Int)
        }
        fun setOnItemClickListener(clickListener: onItemClickListener){
                cListener = clickListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseAdapter.ViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_exercises, parent, false)
                return ViewHolder(itemView, cListener)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val currentItem = dataList[position]
                holder.rvExName.text = currentItem.exName
                holder.rvExReps.text = "Reps: ${currentItem.exReps.toString()}"
        }

        override fun getItemCount(): Int {
                return dataList.size
        }

        class ViewHolder(itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
                val rvExName : TextView = itemView.findViewById(R.id.txtViewExName)
                val rvExReps : TextView = itemView.findViewById(R.id.txtRepsC)
                init {
                        itemView.setOnClickListener{
                                clickListener.onItemClick(adapterPosition)
                        }
                }
        }
}