package com.example.poseexercise.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.data.plan.Plan
import com.google.firebase.auth.FirebaseAuth

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
    fun fetchAllPlans() {
        firebaseRepository.fetchPlans { plans ->
            _plans.postValue(plans)
        }
    }

    /** Get plans for a specific day */
    fun getPlanByDay(day: String) {
        firebaseRepository.fetchPlansByDay(day) { plans ->
            _plans.postValue(plans)
        }
    }

    /** Get incomplete plans for a specific day */
    fun getNotCompletePlans(day: String) {
        firebaseRepository.fetchPlansByDay(day) { plans ->
            val notCompleted = plans.filter { !it.completed }
            _plans.postValue(notCompleted)
        }
    }
}
