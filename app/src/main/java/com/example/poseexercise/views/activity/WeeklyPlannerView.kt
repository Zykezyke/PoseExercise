package com.example.poseexercise

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class WeeklyPlannerView : AppCompatActivity() {

    private lateinit var tvExName: TextView
    private lateinit var tvReps: TextView
    private lateinit var tvSets: TextView
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
        tvSets = findViewById(R.id.tvSets)
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
        tvSets.text = intent.getStringExtra("exSets")
    }

    private fun openUpdateDialog(exId: String, exName: String) {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.activity_weekly_planner_update, null)

        // Initialize views from dialog
        val edtExName = dialogView.findViewById<EditText>(R.id.edtExNameUpd)
        val spinnerReps = dialogView.findViewById<Spinner>(R.id.repsSpinUpd)
        val spinnerSets = dialogView.findViewById<Spinner>(R.id.setsSpinUpd)
        val btnSave = dialogView.findViewById<Button>(R.id.saveBtnUpd)
        val btnBack = dialogView.findViewById<ImageView>(R.id.backBtnUpd)

        // Setup spinners
        val repsArray = (1..30).map { it.toString() }
        val setsArray = (1..30).map { it.toString() }

        val repsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, repsArray)
        val setsAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, setsArray)

        repsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        setsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerReps.adapter = repsAdapter
        spinnerSets.adapter = setsAdapter

        // Set current values
        edtExName.setText(intent.getStringExtra("exName"))

        // Convert string to int and subtract 1 for 0-based index
        val currentReps = intent.getStringExtra("exReps")?.toIntOrNull() ?: 1
        val currentSets = intent.getStringExtra("exSets")?.toIntOrNull() ?: 1

        spinnerReps.setSelection(currentReps - 1)
        spinnerSets.setSelection(currentSets - 1)

        dialog.setView(dialogView)
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        btnSave.setOnClickListener {
            val updatedExercise = mapOf(
                "exId" to exId,
                "exName" to edtExName.text.toString(),
                "exReps" to spinnerReps.selectedItem.toString(),
                "exSets" to spinnerSets.selectedItem.toString()
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
                .child(exId)

            databaseReference.updateChildren(exerciseData.toMap())
                .addOnSuccessListener {
                    Toast.makeText(this, "Exercise Updated Successfully", Toast.LENGTH_SHORT).show()
                    // Update the UI
                    tvExName.text = exerciseData["exName"]
                    tvReps.text = exerciseData["exReps"]
                    tvSets.text = exerciseData["exSets"]
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error updating exercise: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Invalid exercise ID or user not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}