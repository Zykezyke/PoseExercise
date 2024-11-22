package com.example.poseexercise

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.RadioGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import android.os.Handler
import android.os.Looper
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri

class Timer : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 20000 // Default 20 seconds
    private var isTimerRunning = false
    private var originalTime: Long = 20000

    // View declarations
    private lateinit var btnBack: ImageView
    private lateinit var exerciseTitle: TextView
    private lateinit var timerText: TextView
    private lateinit var changeDurationButton: MaterialCardView
    private lateinit var pauseButton: MaterialCardView
    private lateinit var pauseButtonText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()

        val exerciseName = intent.getStringExtra("exercise_name") ?: "Exercise"
        val exerciseImageRes = intent.getIntExtra("exercise_image", R.drawable.dref)

        // Set data to views
        exerciseTitle.text = exerciseName
        findViewById<ImageView>(R.id.exerciseImage).setImageResource(exerciseImageRes)

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
                originalTime = seconds * 1000L
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
            }

        }.start()

        isTimerRunning = true
        pauseButtonText.text = "PAUSE"
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


    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}