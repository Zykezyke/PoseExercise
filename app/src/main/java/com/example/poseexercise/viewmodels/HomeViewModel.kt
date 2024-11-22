package com.example.poseexercise.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.data.plan.Plan
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository: FirebaseRepository
    private val _plans = MutableLiveData<List<Plan>>()

    val plans: LiveData<List<Plan>> get() = _plans

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
        firebaseRepository = FirebaseRepository(userId)
    }

    /** Fetch all plans */
    suspend fun fetchAllPlans() {
        withContext(Dispatchers.IO) {
            val allPlans = firebaseRepository.fetchPlans()
            _plans.postValue(allPlans)
        }
    }

    // Fetch all plans for the current day
    suspend fun getPlanByDay(day: String): List<Plan> {
        return withContext(Dispatchers.IO) {
            firebaseRepository.fetchPlansByDay(day)
        }
    }

    // Fetch not completed plans for the current day
    suspend fun getNotCompletePlans(day: String): MutableList<Plan> {
        return withContext(Dispatchers.IO) {
            val plansByDay = firebaseRepository.fetchPlansByDay(day)
            val notCompletedPlans = plansByDay.filter { !it.completed }.toMutableList()
            Log.d("HomeFragment", "Not completed plans: $notCompletedPlans")
            notCompletedPlans
        }
    }
}
