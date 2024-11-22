package com.example.poseexercise

import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.poseexercise.Login
import com.example.poseexercise.R
import com.example.poseexercise.views.activity.JournalActivity
import com.example.poseexercise.views.activity.PlannerActivity
import com.example.poseexercise.views.fragment.HomeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text

class Home : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var activityTracker: Notification
    private lateinit var cardWorkout: CardView
    private lateinit var cardJournal: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        activityTracker = Notification(this)

        val settingsButton = findViewById<ImageView>(R.id.btnSettings)
        val BMIButton = findViewById<CardView>(R.id.BMIButton)
        val weeklyplannerButton = findViewById<CardView>(R.id.btnWeeklyPlanner)
        val tvName = findViewById<TextView>(R.id.tvName)

        WorkoutReminder(this).scheduleWorkoutCheck()

        settingsButton.setOnClickListener{
            val intent = Intent(this, com.example.poseexercise.Settings::class.java)
            startActivity(intent)
        }

        auth.currentUser?.let { user ->
            val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            val userRef = database.getReference("UserAccounts").child(user.uid)

            userRef.child("userName").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.value as? String
                    tvName.text = userName
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        BMIButton.setOnClickListener{
            val intent = Intent(this, BmiCalculator::class.java)
            startActivity(intent)
        }

        weeklyplannerButton.setOnClickListener{
            val intent = Intent(this, WeeklyPlannerDisplay::class.java)
            startActivity(intent)
        }

        cardWorkout = findViewById(R.id.cardWorkout)

        cardWorkout.setOnClickListener {
            val intent = Intent(this, PlannerActivity::class.java)
            startActivity(intent)
        }

        cardJournal = findViewById(R.id.cardJournal)

        cardJournal.setOnClickListener {
            val intent = Intent(this, JournalActivity::class.java)
            startActivity(intent)
        }

        if (auth.currentUser == null) {
            // Redirect to Login if user is not logged in
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission()
        }

    }
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activityTracker.updateLastActiveTimestamp()
    }
}