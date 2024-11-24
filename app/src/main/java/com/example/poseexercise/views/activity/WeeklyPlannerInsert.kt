package com.example.poseexercise

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.data.database.FirebaseRepository
import com.example.poseexercise.views.activity.Exercises
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch

class WeeklyPlannerInsert : AppCompatActivity() {

    private lateinit var btnAdd: Button
    private lateinit var btnBack: ImageView
    private lateinit var exRecyclerView: RecyclerView
    private lateinit var dataList: ArrayList<Exercises>
    private lateinit var adapter: ExerciseAdapter
    private lateinit var selectedDay: String
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_planner_insert)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firebaseRepository = FirebaseRepository(currentUser.uid)

        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.backBtn2)
        exRecyclerView = findViewById(R.id.wpRecycler)
        selectedDay = intent.getStringExtra("SELECTED_DAY") ?: "Sunday"
        val txtDay = findViewById<TextView>(R.id.txtDay)
        txtDay.text = selectedDay.uppercase()

        dataList = arrayListOf()
        adapter = ExerciseAdapter(dataList)
        exRecyclerView.adapter = adapter
        exRecyclerView.layoutManager = LinearLayoutManager(this)

        getExerciseData()

        btnAdd.setOnClickListener {
            val intent = Intent(this, WeeklyPlannerAdd::class.java)
            intent.putExtra("SELECTED_DAY", selectedDay)  // Make sure we're passing the day
            startActivity(intent)
            finish()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, WeeklyPlannerDisplay::class.java)
            startActivity(intent)
        }
    }

    private fun getExerciseData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("UserAccounts")
                .child(currentUser.uid)
                .child("${selectedDay}Planner")
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Launch a coroutine to fetch workout results
        lifecycleScope.launch {
            try {
                val workoutResults = firebaseRepository.fetchThisWeeksWorkoutResults()

                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        dataList.clear()

                        if (snapshot.exists()) {
                            for (dataSnap in snapshot.children) {
                                val exerData = dataSnap.getValue(Exercises::class.java)
                                exerData?.let { dataList.add(it) }
                            }

                            // Update adapter with the workout results
                            (exRecyclerView.adapter as ExerciseAdapter).updateWorkoutResults(workoutResults)

                            adapter.setOnItemClickListener(object : ExerciseAdapter.onItemClickListener {
                                override fun onItemClick(position: Int) {
                                    val intent = Intent(this@WeeklyPlannerInsert, WeeklyPlannerView::class.java)
                                    intent.putExtra("exId", dataList[position].exId)
                                    intent.putExtra("exName", dataList[position].exName)
                                    intent.putExtra("exReps", dataList[position].exReps)
                                    intent.putExtra("SELECTED_DAY", selectedDay)
                                    startActivity(intent)
                                }
                            })

                            exRecyclerView.visibility = View.VISIBLE
                        } else {
                            val noActivityMessage = findViewById<TextView>(R.id.no_activity_message)
                            noActivityMessage.text = getString(R.string.no_activities_yet)
                            noActivityMessage.isVisible = true
                            exRecyclerView.visibility = View.GONE
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@WeeklyPlannerInsert, "Failed to load data.", Toast.LENGTH_SHORT)
                            .show()
                    }
                })
            } catch (e: Exception) {
                Toast.makeText(this@WeeklyPlannerInsert, "Failed to fetch workout results.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}