package com.example.poseexercise.views.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.Home
import com.example.poseexercise.R
import com.example.poseexercise.adapters.ExerciseAdapter
import com.example.poseexercise.data.plan.Constants
import com.example.poseexercise.util.MemoryManagement
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Displays a [RecyclerView] of exercise types.
 */
class PlanStepOneFragment : Fragment(), MemoryManagement {
    private val exerciseList = Constants.getExerciseList()
    private lateinit var btnBack: ImageView
    private var searchQuery: CharSequence? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_step_one, container, false)
        val activity = activity as Context
        // Get all the layout item
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        val levelGroup: ChipGroup = view.findViewById(R.id.chip_group)
        val adapter = ExerciseAdapter(activity, findNavController(this))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.setExercises(exerciseList)

        val btnBack = view.findViewById<ImageView>(R.id.btnBack)

        // Set OnClickListener for back button
        btnBack.setOnClickListener {
            requireActivity().finish()
        }

        // Set on click listener for chip
        levelGroup.isSingleSelection = true
        levelGroup.setOnCheckedStateChangeListener { chipGroup, _ ->
            // Get the chip from the checked chip id
            val checkedChipId = chipGroup.checkedChipId
            val chip = chipGroup.findViewById<Chip>(checkedChipId)
            // Filter the result based on the selected chip
            if (chip != null) {
                searchQuery = chip.text
                adapter.filter.filter(searchQuery)
            } else {
                adapter.setExercises(exerciseList)
            }
        }

        // Handle back press to navigate to Home activity
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val intent = Intent(requireActivity(), Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    requireActivity().finish() // Finish current activity
                }
            }
        )
        // Inflate the layout for this fragment
        return view
    }

    override fun clearMemory() {
        searchQuery = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }

}