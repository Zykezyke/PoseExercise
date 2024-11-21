package com.example.poseexercise.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.data.database.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResultViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository: FirebaseRepository

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
        firebaseRepository = FirebaseRepository(userId)
    }

    /** Insert a workout result */
    suspend fun insert(result: WorkoutResult) {
        withContext(Dispatchers.IO) {
            firebaseRepository.saveWorkoutResult(result)
        }
    }

    /** Get all workout results */
    suspend fun getAllResult(): List<WorkoutResult>? {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<WorkoutResult>()
            firebaseRepository.fetchAllWorkoutResults { results ->
                result.addAll(results)
            }
            result
        }
    }

    /** Get the most recent workout results */
    suspend fun getRecentWorkout(limit: Int): List<WorkoutResult>? {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<WorkoutResult>()
            firebaseRepository.fetchRecentWorkoutResults(limit) { results ->
                result.addAll(results)
            }
            result
        }
    }
}
