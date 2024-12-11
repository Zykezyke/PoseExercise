package com.formfix.poseexercise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.formfix.poseexercise.data.plan.Constants
import com.formfix.poseexercise.views.activity.Exercises
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WeeklyPlannerAdd : AppCompatActivity() {

    private lateinit var backBtn: ImageView
    private lateinit var exerciseSpinner: Spinner

    private lateinit var repsInput: EditText
    private lateinit var saveButton: Button
    private lateinit var selectedDay: String
    private lateinit var increaseButton: ImageButton
    private lateinit var decreaseButton: ImageButton
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_planner_add)

        selectedDay = intent.getStringExtra("SELECTED_DAY") ?: "Sunday"

        // Initialize views
        backBtn = findViewById(R.id.backBtn3)
        exerciseSpinner = findViewById(R.id.edtExName) // We'll modify the layout to change EditText to Spinner
        increaseButton = findViewById(R.id.increaseButton)
        decreaseButton = findViewById(R.id.decreaseButton)
        repsInput = findViewById(R.id.repsInput)
        saveButton = findViewById(R.id.saveBtn)

        Log.d("WeeklyPlanner", "Add - Received selected day: $selectedDay")

        repsInput.setText("1");
        setupDatabaseReference()
        setupSpinners()
        setupClickListeners()
    }

    private fun setupDatabaseReference() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d("WeeklyPlanner", "Add - Setting up database reference for day: $selectedDay")
            databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("UserAccounts")
                .child(currentUser.uid)
                .child("${selectedDay}Planner")
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupSpinners() {
        // Setup exercise names spinner
        val exerciseNames = Constants.getExerciseList().map { it.name }
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        exerciseSpinner.adapter = exerciseAdapter
    }

    private fun setupClickListeners() {
        backBtn.setOnClickListener {
            val intent = Intent(this, WeeklyPlannerInsert::class.java)
            intent.putExtra("SELECTED_DAY", selectedDay)
            startActivity(intent)
        }

        saveButton.setOnClickListener {
            saveExercise()
        }

        increaseButton.setOnClickListener {
            val currentValue = repsInput.text.toString().toIntOrNull() ?: 0
            if (currentValue < 999) {
                repsInput.setText((currentValue + 1).toString())
            }
        }

        // Decrease button logic
        decreaseButton.setOnClickListener {
            val currentValue = repsInput.text.toString().toIntOrNull() ?: 0
            if (currentValue > 1) {
                repsInput.setText((currentValue - 1).toString())
            }
        }
    }

    private fun saveExercise() {
        val exerciseName = exerciseSpinner.selectedItem.toString()
        val newReps = repsInput.text.toString().toIntOrNull() ?: 1

        databaseReference.get().addOnSuccessListener { snapshot ->
            var existingExerciseId: String? = null
            var existingReps = 0

            // Check for existing exercise with the same name
            snapshot.children.forEach { child ->
                val exercise = child.getValue(Exercises::class.java)
                if (exercise?.exName == exerciseName) {
                    existingExerciseId = exercise.exId
                    existingReps = exercise.exReps?.toIntOrNull() ?: 0
                    return@forEach
                }
            }

            if (existingExerciseId != null) {
                // Calculate combined reps, capped at 100
                val combinedReps = minOf(existingReps + newReps, 999)

                // Update existing exercise with new reps
                val updatedExercise = Exercises(existingExerciseId!!, exerciseName, combinedReps.toString())

                databaseReference.child(existingExerciseId!!).setValue(updatedExercise)
                    .addOnSuccessListener {
                        Log.d("Update Success", "Exercise updated successfully: $existingExerciseId")
                        Toast.makeText(this, "Exercise Updated Successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, WeeklyPlannerInsert::class.java)
                        intent.putExtra("SELECTED_DAY", selectedDay)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { err ->
                        Log.e("FirebaseError", "Error: ${err.message}")
                        Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // No existing exercise found, create new one
                val exId = databaseReference.push().key!!
                val exercise = Exercises(exId, exerciseName, newReps.toString())

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
        }.addOnFailureListener { err ->
            Log.e("FirebaseError", "Error reading data: ${err.message}")
            Toast.makeText(this, "Error reading data: ${err.message}", Toast.LENGTH_SHORT).show()
        }
    }
}