package com.example.poseexercise

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.views.activity.Exercises
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class WeeklyPlannerAdd : AppCompatActivity() {

    private lateinit var backBtn : ImageView
    private lateinit var edtExName : EditText
    private lateinit var repsSpin : Spinner
    private lateinit var setsSpin : Spinner
    private lateinit var saveButton : Button
    private lateinit var selectedDay: String

    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_planner_add)
        selectedDay = intent.getStringExtra("SELECTED_DAY") ?: "Sunday"
        backBtn = findViewById(R.id.backBtn3)
        edtExName = findViewById(R.id.edtExName)
        repsSpin = findViewById(R.id.repsSpin)
        setsSpin = findViewById(R.id.setsSpin)
        saveButton = findViewById(R.id.saveBtn)
        Log.d("WeeklyPlanner", "Add - Received selected day: $selectedDay")

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("WeeklyPlanner", "Add - Setting up database reference for day: $selectedDay")
            // Set the database reference path to UserAccounts/UID/SundayPlanner
            databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("UserAccounts")
                .child(currentUser.uid)
                .child("${selectedDay}Planner")
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        val numArray = (1..30).toList()

        val adapterReps = ArrayAdapter(this, android.R.layout.simple_spinner_item, numArray)
        val adapterSets = ArrayAdapter(this, android.R.layout.simple_spinner_item, numArray)

        adapterReps.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapterSets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        repsSpin.adapter = adapterReps
        setsSpin.adapter = adapterSets

        backBtn.setOnClickListener {
            val intent = Intent(this, WeeklyPlannerInsert::class.java)
            intent.putExtra("SELECTED_DAY", selectedDay)  // Pass the selected day back
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            if (edtExName.text.toString().isEmpty()) {
                edtExName.error = "Please enter an Exercise Name"
            } else {
                saveExercise()
            }
        }
    }

    private fun saveExercise() {
        // Get Values
        val exerciseName = edtExName.text.toString()
        val reps = repsSpin.selectedItemPosition.inc()
        val sets = setsSpin.selectedItemPosition.inc()

        val exId = databaseReference.push().key!!
        val exercise = Exercises(exId, exerciseName, reps.toString(), sets.toString())

        Log.d("exId", "Generated ID: $exId")
        Toast.makeText(this, "Saving Exercise...", Toast.LENGTH_SHORT).show()

        databaseReference.child(exId).setValue(exercise)
            .addOnSuccessListener {
                Log.d("Add Success ", "Exercise saved successfully: $exId")
                Toast.makeText(this, "Exercise Added Successfully", Toast.LENGTH_SHORT).show()


                val intent = Intent(this, WeeklyPlannerInsert::class.java)
                intent.putExtra("SELECTED_DAY", selectedDay)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { err ->

                Log.e("FirebaseError", "Error: ${err.message}")
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()

            }
    }

}