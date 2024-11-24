package com.example.poseexercise.views.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.example.poseexercise.Home
import com.example.poseexercise.R
import com.example.poseexercise.data.database.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * CancelFragment: A fragment displayed when the user cancels an operation.
 *
 * This fragment provides a button to navigate back to the home screen using the Navigation component.
 */
class CancelFragment : Fragment() {
    private lateinit var navigateToHomeButton: Button

    private lateinit var repository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User not logged in")
        repository = FirebaseRepository(userId)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cancel, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateToHomeButton = view.findViewById(R.id.goToHomeFromCancel)

        // Set up click listener to navigate to the home screen
        navigateToHomeButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    repository.deleteAllPlans()
                    println("All plans deleted successfully")
                } catch (e: Exception) {
                    println("Failed to delete plans: $e")
                } finally {
                    navigateToHome()
                }
            }
        }

        // Handle back press to navigate to Home activity
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            repository.deleteAllPlans()
                            println("All plans deleted successfully")
                        } catch (e: Exception) {
                            println("Failed to delete plans: $e")
                        } finally {
                            navigateToHome()
                        }
                    }
                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAllPlans()
                println("All plans deleted successfully")
            } catch (e: Exception) {
                println("Failed to delete plans: $e")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                repository.deleteAllPlans()
                println("All plans deleted successfully")
            } catch (e: Exception) {
                println("Failed to delete plans: $e")
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), Home::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}