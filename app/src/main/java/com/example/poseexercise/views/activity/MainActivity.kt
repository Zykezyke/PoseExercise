package com.example.poseexercise.views.activity

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.poseexercise.Home
import com.example.poseexercise.Login
import com.example.poseexercise.R
import com.example.poseexercise.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import np.com.susanthapa.curved_bottom_navigation.CbnMenuItem

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var prefManager: PrefManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().setLanguageCode("en");
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        prefManager = PrefManager(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Check if user is logged in
        if (auth.currentUser == null) {
            // Redirect to Login if not logged in
            startActivity(Intent(this, Login::class.java))
            finish()
            return
        } else {
            // Direct to Home Activity if logged in
            startActivity(Intent(this, Home::class.java))
            finish()
            return
        }

//        // Handle onboarding if needed
//        if (prefManager.isFirstTimeLaunch()) {
//            startActivity(Intent(this, OnboardingActivity::class.java))
//            finish()
//            return
//        }
//
//        increaseNotificationVolume()
//
//        // Set up navigation controller for fragments (if necessary later)
//        val navHostFragment = supportFragmentManager
//            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHostFragment.navController
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    companion object {
        var workoutResultData: String? = null
        var workoutTimer: String? = null
    }

    private fun increaseNotificationVolume() {
        // Increase the volume to max.
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_NOTIFICATION,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION),
            0
        )
    }
}