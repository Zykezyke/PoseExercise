package com.example.poseexercise.data.database

import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.results.WorkoutResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseRepository(private val userId: String) {

    private val databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("UserAccounts")
        .child(userId)

    fun savePlan(plan: Plan): String {
        val planId = databaseReference.child("plans").push().key ?: return ""
        plan.id = planId // Set the ID before saving
        databaseReference.child("plans").child(planId).setValue(plan)
        return planId
    }

    suspend fun fetchPlans(): List<Plan> = suspendCancellableCoroutine { cont ->
        databaseReference.child("plans").get()
            .addOnSuccessListener { snapshot ->
                val plans = snapshot.children.mapNotNull { it.getValue(Plan::class.java) }
                cont.resume(plans)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    suspend fun deletePlan(planId: String) = suspendCancellableCoroutine<Unit> { cont ->
        databaseReference.child("plans").child(planId).removeValue()
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun fetchPlansByDay(day: String): List<Plan> {
        val allPlans = fetchPlans()
        return allPlans.filter { it.selectedDays.contains(day) }
    }

    suspend fun updatePlan(planId: String, updatedPlan: Plan) = suspendCancellableCoroutine<Unit> { cont ->
        databaseReference.child("plans").child(planId).setValue(updatedPlan)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }


    suspend fun updatePlanCompletion(completed: Boolean, time: Long?, planId: String) = suspendCancellableCoroutine<Unit> { cont ->
        val updates = mapOf("completed" to completed, "timeCompleted" to time)
        databaseReference.child("plans").child(planId).updateChildren(updates)
            .addOnSuccessListener { cont.resume(Unit) }
            .addOnFailureListener { cont.resumeWithException(it) }
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

