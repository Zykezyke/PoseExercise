package com.example.poseexercise.data.database

import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.results.WorkoutResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseRepository(private val userId: String) {

    private val databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("UserAccounts")
        .child(userId)

    fun savePlan(plan: Plan) {
        val planId = databaseReference.child("plans").push().key ?: return
        databaseReference.child("plans").child(planId).setValue(plan)
    }

    fun fetchPlans(callback: (List<Plan>) -> Unit) {
        databaseReference.child("plans").get().addOnSuccessListener { snapshot ->
            val plans = snapshot.children.mapNotNull { it.getValue(Plan::class.java) }
            callback(plans)
        }
    }

    fun deletePlan(planId: String) {
        databaseReference.child("plans").child(planId).removeValue()
    }

    fun fetchPlansByDay(day: String, callback: (List<Plan>) -> Unit) {
        fetchPlans { plans ->
            val filteredPlans = plans.filter { it.selectedDays.contains(day) }
            callback(filteredPlans)
        }
    }

    fun updatePlan(planId: String, updatedPlan: Plan) {
        databaseReference.child("plans").child(planId).setValue(updatedPlan)
    }


    fun updatePlanCompletion(planId: String, completed: Boolean, time: Long?) {
        val updates = mapOf("completed" to completed, "timeCompleted" to time)
        databaseReference.child("plans").child(planId).updateChildren(updates)
    }

    fun saveWorkoutResult(result: WorkoutResult) {
        val resultId = databaseReference.child("workoutResults").push().key ?: return
        databaseReference.child("workoutResults").child(resultId).setValue(result)
    }

    fun fetchAllWorkoutResults(callback: (List<WorkoutResult>) -> Unit) {
        databaseReference.child("workoutResults").get().addOnSuccessListener { snapshot ->
            val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
            callback(results)
        }
    }

    fun fetchRecentWorkoutResults(limit: Int, callback: (List<WorkoutResult>) -> Unit) {
        fetchAllWorkoutResults { results ->
            val recentResults = results.sortedByDescending { it.timestamp }.take(limit)
            callback(recentResults)
        }
    }

    fun updateWorkoutField(resultId: String, fieldName: String, value: Any) {
        databaseReference.child("workoutResults").child(resultId).child(fieldName).setValue(value)
    }


}

