package com.example.poseexercise

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.poseexercise.data.plan.Constants
import com.example.poseexercise.views.activity.Exercises
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class WeeklyPlannerView : AppCompatActivity() {

    private lateinit var tvExName: TextView
    private lateinit var tvReps: TextView
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private lateinit var btnBack: ImageView
    private lateinit var selectedDay: String
    private lateinit var tvDay : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weekly_planner_view)

        selectedDay = intent.getStringExtra("SELECTED_DAY") ?: "Sunday"
        val txtDay = findViewById<TextView>(R.id.txtDay)
        txtDay.text = selectedDay.uppercase()

        initializeViews()
        setValuesToView()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.backBtn4)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnDelete = findViewById(R.id.btnDelete)
        tvExName = findViewById(R.id.tvExName)
        tvReps = findViewById(R.id.tvReps)
        tvDay = findViewById(R.id.txtDay)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnUpdate.setOnClickListener {
            openUpdateDialog(
                intent.getStringExtra("exId") ?: "",
                intent.getStringExtra("exName") ?: ""
            )
        }

        btnDelete.setOnClickListener {
            deleteRecord(intent.getStringExtra("exId") ?: "")
        }
    }

    private fun deleteRecord(exId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("UserAccounts")
                .child(currentUser.uid)
                .child("${selectedDay}Planner")

            databaseReference.child(exId).removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Exercise Deleted", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Deleting Error ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setValuesToView() {
        tvExName.text = intent.getStringExtra("exName")
        tvReps.text = intent.getStringExtra("exReps")
    }

    private fun openUpdateDialog(exId: String, exName: String) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.activity_weekly_planner_update, null)

        // Initialize views from dialog
        val spinnerExName = dialogView.findViewById<Spinner>(R.id.edtExNameUpd)
        val spinnerReps = dialogView.findViewById<Spinner>(R.id.repsSpinUpd)
        val btnSave = dialogView.findViewById<Button>(R.id.saveBtnUpd)
        val btnBack = dialogView.findViewById<ImageView>(R.id.backBtnUpd)

        // Setup exercise names spinner
        val exerciseNames = Constants.getExerciseList().map { it.name }
        val exerciseAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseNames)
        exerciseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExName.adapter = exerciseAdapter

        // Set current exercise selection
        val currentExIndex = exerciseNames.indexOf(intent.getStringExtra("exName"))
        if (currentExIndex != -1) {
            spinnerExName.setSelection(currentExIndex)
        }

        // Setup reps spinner
        val repsArray = (1..100).map { it.toString() }
        val repsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repsArray)
        repsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReps.adapter = repsAdapter

        // Set current reps selection
        val currentReps = intent.getStringExtra("exReps")?.toIntOrNull() ?: 1
        spinnerReps.setSelection(currentReps - 1)

        dialog.setView(dialogView)
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnSave.setOnClickListener {
            val updatedExercise = mapOf(
                "exId" to exId,
                "exName" to spinnerExName.selectedItem.toString(),
                "exReps" to spinnerReps.selectedItem.toString()
            )

            updateExerciseData(exId, updatedExercise)
            alertDialog.dismiss()
        }

        btnBack.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun updateExerciseData(exId: String, exerciseData: Map<String, String>) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && exId.isNotEmpty()) {
            val databaseReference = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("UserAccounts")
                .child(currentUser.uid)
                .child("${selectedDay}Planner")

            // First check if there's another exercise with the same name (excluding current exercise)
            databaseReference.get().addOnSuccessListener { snapshot ->
                var existingExerciseId: String? = null
                var existingReps = 0

                snapshot.children.forEach { child ->
                    val exercise = child.getValue(Exercises::class.java)
                    if (exercise != null) {
                        if (exercise?.exName == exerciseData["exName"] && exercise.exId != exId) {
                            existingExerciseId = exercise.exId
                            existingReps = exercise.exReps?.toIntOrNull() ?: 0
                            return@forEach
                        }
                    }
                }

                if (existingExerciseId != null) {
                    // Combine exercises
                    val newReps = exerciseData["exReps"]?.toIntOrNull() ?: 0
                    val combinedReps = minOf(existingReps + newReps, 100)

                    // Update existing exercise
                    val updatedExercise = mapOf(
                        "exId" to existingExerciseId!!,
                        "exName" to exerciseData["exName"]!!,
                        "exReps" to combinedReps.toString()
                    )

                    // Delete the current exercise
                    databaseReference.child(exId).removeValue()

                    // Update the existing exercise with combined reps
                    databaseReference.child(existingExerciseId!!).updateChildren(updatedExercise.toMap())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Exercises Combined Successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Error combining exercises: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // No existing exercise with same name, proceed with normal update
                    databaseReference.child(exId).updateChildren(exerciseData.toMap())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Exercise Updated Successfully", Toast.LENGTH_SHORT).show()
                            tvExName.text = exerciseData["exName"]
                            tvReps.text = exerciseData["exReps"]
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Error updating exercise: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { error ->
                Toast.makeText(this, "Error reading data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid exercise ID or user not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}