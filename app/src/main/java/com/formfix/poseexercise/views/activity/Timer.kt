package com.formfix.poseexercise

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import android.os.Handler
import android.os.Looper
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.formfix.poseexercise.data.results.WorkoutResult
import com.formfix.poseexercise.viewmodels.ResultViewModel
import com.formfix.poseexercise.util.MyUtils
import kotlinx.coroutines.launch

class Timer : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 20000 // Default 20 seconds
    private var isTimerRunning = false
    private var originalTime: Long = 20000
    private lateinit var resultViewModel: ResultViewModel
    private var exerciseName: String = "Exercise"
    private var classificationName: String = "Exercise" // Added to store the classification name

    // View declarations
    private lateinit var btnBack: ImageView
    private lateinit var exerciseTitle: TextView
    private lateinit var timerText: TextView
    private lateinit var changeDurationButton: MaterialCardView
    private lateinit var pauseButton: MaterialCardView
    private lateinit var pauseButtonText: TextView
    private lateinit var exerciseImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        resultViewModel = ViewModelProvider(this).get(ResultViewModel::class.java)

        initializeViews()
        setupClickListeners()

        exerciseName = intent.getStringExtra("exercise_name") ?: "Exercise"
        exerciseTitle.text = exerciseName
        // Convert the display name to classification name when receiving it
        classificationName = MyUtils.databaseNameToClassification(exerciseName)


        val exerciseImageRes = intent.getIntExtra("exercise_image", R.drawable.dref)
        val isGif = intent.getBooleanExtra("is_gif", false)

        // Set data to views
        if (isGif) {
            Glide.with(this)
                .asGif()
                .load(exerciseImageRes)
                .into(exerciseImage)
        } else {
            exerciseImage.setImageResource(exerciseImageRes)
        }

        updateTimerText()

        pauseButtonText.text = "START"
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        exerciseTitle = findViewById(R.id.exerciseTitle)
        timerText = findViewById(R.id.timerText)
        changeDurationButton = findViewById(R.id.changeDurationButton)
        pauseButton = findViewById(R.id.pauseButton)
        pauseButtonText = findViewById(R.id.pauseButtonText)
        exerciseImage = findViewById(R.id.exerciseImage)
    }

    private fun setupClickListeners() {
        pauseButton.setOnClickListener {
            if (isTimerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }

        changeDurationButton.setOnClickListener {
            showDurationDialog()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showDurationDialog() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_timer_settings, null)
        val timeInput = dialogView.findViewById<EditText>(R.id.timeInput)

        dialog.setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val seconds = timeInput.text.toString().toIntOrNull() ?: 20
                if (seconds > 3600) {
                    // Show error message for exceeding maximum duration
                    Toast.makeText(
                        this,
                        "Duration cannot exceed 3600 seconds (1 hour). Setting to maximum.",
                        Toast.LENGTH_LONG
                    ).show()
                    originalTime = 3600 * 1000L
                } else {
                    originalTime = seconds * 1000L
                }
                timeLeftInMillis = originalTime
                updateTimerText()
                pauseButton.isEnabled = true
                pauseButtonText.text = "START"
                countDownTimer?.cancel()
                isTimerRunning = false
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun playTimerFinishSound() {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val ringtone: Ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)

        if (ringtone != null) {
            ringtone.play()

            // Stop the alarm after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
            }, 5000)
        } else {
            // Fallback to notification sound if no alarm sound is set
            val notificationUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationRingtone: Ringtone = RingtoneManager.getRingtone(applicationContext, notificationUri)
            if (notificationRingtone != null) {
                notificationRingtone.play()

                // Stop the notification sound after 5 seconds
                Handler(Looper.getMainLooper()).postDelayed({
                    if (notificationRingtone.isPlaying) {
                        notificationRingtone.stop()
                    }
                }, 5000)
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 10) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isTimerRunning = false
                pauseButtonText.text = "START"
                timeLeftInMillis = originalTime
                updateTimerText()
                playTimerFinishSound()
                saveWorkoutResult()
                showCongratsMessage()
            }
        }.start()

        isTimerRunning = true
        pauseButtonText.text = "PAUSE"
    }

    private fun saveWorkoutResult() {
        // Calculate workout time in minutes from the original time (converting from milliseconds)
        val workoutTimeInMin = originalTime / (1000.0 * 60.0)

        val workoutResult = WorkoutResult(
            id = 0, // Default value
            exerciseName = classificationName, // Use the classification name instead of display name
            repeatedCount = 0, // Default value
            confidence = 0f, // Default value
            timestamp = System.currentTimeMillis(),
            calorie = 0.0, // Default value
            workoutTimeInMin = workoutTimeInMin
        )

        lifecycleScope.launch {
            resultViewModel.insert(workoutResult)
        }
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        pauseButtonText.text = "RESUME"
    }

    private fun updateTimerText() {
        val minutes = ((timeLeftInMillis / 1000) / 60).toInt()
        val seconds = ((timeLeftInMillis / 1000) % 60).toInt()
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun showCongratsMessage() {
        Toast.makeText(this, "Congratulations! You've completed $exerciseName. Great job!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}