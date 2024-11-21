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

class Timer : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 20000 // Default 20 seconds
    private var isTimerRunning = false
    private var originalTime: Long = 20000
    private var isCountingReps = false
    private var targetReps = 20 // Default reps

    // View declarations
    private lateinit var btnBack: ImageView
    private lateinit var btnSettings: ImageView
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
        updateTimerText()

        pauseButtonText.text = "START"
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnSettings = findViewById(R.id.btnSettings)
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

        btnSettings.setOnClickListener {
            val intent = Intent(this, com.example.poseexercise.Settings::class.java)
            startActivity(intent)
        }
    }

    private fun showDurationDialog() {
        val dialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_timer_settings, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
        val timeInput = dialogView.findViewById<EditText>(R.id.timeInput)
        val repsInput = dialogView.findViewById<EditText>(R.id.repsInput)

        timeInput.isEnabled = true
        repsInput.isEnabled = false

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioTime -> {
                    timeInput.isEnabled = true
                    repsInput.isEnabled = false
                }
                R.id.radioReps -> {
                    timeInput.isEnabled = false
                    repsInput.isEnabled = true
                }
            }
        }

        dialog.setView(dialogView)
            .setTitle("Set Timer Type")
            .setPositiveButton("OK") { _, _ ->
                when (radioGroup.checkedRadioButtonId) {
                    R.id.radioTime -> {
                        isCountingReps = false
                        val seconds = timeInput.text.toString().toIntOrNull() ?: 20
                        originalTime = seconds * 1000L
                        timeLeftInMillis = originalTime
                        updateTimerText()
                        pauseButton.isEnabled = true
                        pauseButtonText .text = "START"
                        countDownTimer?.cancel()
                        isTimerRunning = false
                    }
                    R.id.radioReps -> {
                        isCountingReps = true
                        targetReps = repsInput.text.toString().toIntOrNull() ?: 20
                        updateRepsDisplay()
                        pauseButton.isEnabled = true
                        pauseButtonText.text = "DONE"
                        countDownTimer?.cancel()
                        isTimerRunning = false
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startTimer() {
        if (isCountingReps) {
            return
        }

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
                //playTimerFinishSound()
            }
        }.start()

        isTimerRunning = true
        pauseButtonText.text = "PAUSE"
    }

    private fun pauseTimer() {
        if (isCountingReps) {
            return
        }

        countDownTimer?.cancel()
        isTimerRunning = false
        pauseButtonText.text = "RESUME"
    }

    private fun updateTimerText() {
        if (isCountingReps) {
            updateRepsDisplay()
            return
        }

        val minutes = ((timeLeftInMillis / 1000) / 60).toInt()
        val seconds = ((timeLeftInMillis / 1000) % 60).toInt()
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateRepsDisplay() {
        timerText.text = "$targetReps"
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}