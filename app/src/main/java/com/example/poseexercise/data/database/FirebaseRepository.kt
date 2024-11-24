package com.example.poseexercise.data.database

import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.results.WorkoutResult
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Calendar
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

    suspend fun fetchAllWorkoutResults(): List<WorkoutResult> = suspendCancellableCoroutine { cont ->
        databaseReference.child("workoutResults").get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
                cont.resume(results)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }


    suspend fun fetchRecentWorkoutResults(limit: Int): List<WorkoutResult> = suspendCancellableCoroutine { cont ->
        databaseReference.child("workoutResults").get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
                    .sortedByDescending { it.timestamp }
                    .take(limit)
                cont.resume(results)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    suspend fun fetchThisWeeksWorkoutResults(): List<WorkoutResult> = suspendCancellableCoroutine { cont ->
        val calendar = Calendar.getInstance()
        calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
        calendar.set(Calendar.DAY_OF_WEEK - 1, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startOfWeek = calendar.timeInMillis

        databaseReference.child("workoutResults").get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
                    .filter {
                        it.timestamp >= startOfWeek && !isZeroResult(it)
                    }
                cont.resume(results)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    suspend fun fetchLastTwoWeeksWorkoutResults(): List<WorkoutResult> = suspendCancellableCoroutine { cont ->
        val calendar = Calendar.getInstance()
        calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
        calendar.add(Calendar.DAY_OF_YEAR, -14) // Go back 14 days
        val startTime = calendar.timeInMillis

        databaseReference.child("workoutResults").get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
                    .filter {
                        it.timestamp >= startTime
                    }
                cont.resume(results)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    suspend fun fetchWithTimerWorkoutResults(): List<WorkoutResult> = suspendCancellableCoroutine { cont ->
        val calendar = Calendar.getInstance()
        calendar.timeZone = java.util.TimeZone.getTimeZone("UTC+8")
        calendar.add(Calendar.DAY_OF_YEAR, -14) // Go back 14 days
        val startTime = calendar.timeInMillis

        databaseReference.child("workoutResults").get()
            .addOnSuccessListener { snapshot ->
                val results = snapshot.children.mapNotNull { it.getValue(WorkoutResult::class.java) }
                    .filter {
                        it.timestamp >= startTime // Filter out zero results
                    }
                cont.resume(results)
            }
            .addOnFailureListener { exception ->
                cont.resumeWithException(exception)
            }
    }

    private fun isZeroResult(workoutResult: WorkoutResult): Boolean {
        return workoutResult.calorie == 0.0 && workoutResult.repeatedCount == 0 && workoutResult.confidence == 0f
    }


    fun updateWorkoutField(resultId: String, fieldName: String, value: Any) {
        databaseReference.child("workoutResults").child(resultId).child(fieldName).setValue(value)
    }

    suspend fun deleteAllPlans() {
        val plans = fetchPlans()
        println("Plans fetched for deletion: $plans") // Log the fetched plans
        plans.forEach {
            println("Deleting plan with ID: ${it.id}") // Log each plan ID
            deletePlan(it.id)
        }
    }
}

