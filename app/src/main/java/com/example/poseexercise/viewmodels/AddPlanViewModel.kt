package com.example.poseexercise.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.database.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository: FirebaseRepository

    init {
        // Get current user id
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
        firebaseRepository = FirebaseRepository(userId)
    }

    /** Insert a plan */
    fun insert(plan: Plan) {
        viewModelScope.launch {
            firebaseRepository.savePlan(plan)
        }
    }

    /** Update a plan */
    fun update(planId: String, updatedPlan: Plan) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.updatePlan(planId, updatedPlan)
        }
    }

    /** Update only complete state and timeCompleted of a plan */
    fun updateComplete(completedState: Boolean, timeComplete: Long?, planId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.updatePlanCompletion(completedState, timeComplete, planId)
        }
    }

    /** Delete a plan */
    fun deletePlan(planId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepository.deletePlan(planId)
        }
    }
}
