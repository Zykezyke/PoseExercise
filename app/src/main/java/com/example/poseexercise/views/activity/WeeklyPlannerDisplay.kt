package com.example.poseexercise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class WeeklyPlannerDisplay : AppCompatActivity() {
    private lateinit var sunBtn: Button
    private lateinit var monBtn: Button
    private lateinit var tuesBtn: Button
    private lateinit var wedBtn: Button
    private lateinit var thursBtn: Button
    private lateinit var friBtn: Button
    private lateinit var satBtn: Button
    private lateinit var backBtn: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_planner_display)

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        databaseReference = FirebaseDatabase.getInstance().getReference("WeeklyPlanner")
        auth = FirebaseAuth.getInstance()

        sunBtn = findViewById(R.id.btnSunday)
        monBtn = findViewById(R.id.btnMonday)
        tuesBtn = findViewById(R.id.btnTuesday)
        wedBtn = findViewById(R.id.btnWednesday)
        thursBtn = findViewById(R.id.btnThursday)
        friBtn = findViewById(R.id.btnFriday)
        satBtn = findViewById(R.id.btnSaturday)
        backBtn = findViewById(R.id.backBtn1)
    }

    private fun setupClickListeners() {
        val dayButtons = mapOf(
            sunBtn to "Sunday",
            monBtn to "Monday",
            tuesBtn to "Tuesday",
            wedBtn to "Wednesday",
            thursBtn to "Thursday",
            friBtn to "Friday",
            satBtn to "Saturday"
        )

        dayButtons.forEach { (button, day) ->
            button.setOnClickListener {
                val intent = Intent(this, WeeklyPlannerInsert::class.java).apply {
                    putExtra("SELECTED_DAY", day)
                }
                Log.d("WeeklyPlanner", "Display - Selected day: $day")
                startActivity(intent)
            }
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }
    }
}