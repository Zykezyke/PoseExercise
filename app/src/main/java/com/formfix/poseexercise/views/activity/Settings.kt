package com.formfix.poseexercise

import android.content.SharedPreferences
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth


class Settings : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val notificationsCard = findViewById<Button>(R.id.notificationsCard)
        val cameraPermissionCard = findViewById<Button>(R.id.cameraPermissionCard)
        val aboutUsCard = findViewById<Button>(R.id.aboutUsCard)
        val privacyPolicyCard = findViewById<Button>(R.id.privacyPolicyCard)
        val logoutCard = findViewById<Button>(R.id.logoutCard)
        val profileButton = findViewById<Button>(R.id.btnProfile)

        //darkmode
        val toggleDarkMode = findViewById<Switch>(R.id.darkModeSwitch)

        sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE)

// Get the saved preference, defaulting to system theme if not set
        val isNightMode = sharedPreferences.getBoolean("isNightMode", isSystemInNightMode())
        toggleDarkMode.isChecked = isNightMode

        toggleDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            AppCompatDelegate.setDefaultNightMode(mode)
            saveThemeState(isChecked)
            overridePendingTransition(0, 0)
        }


        backButton.setOnClickListener{
            finish()
        }

        profileButton.setOnClickListener{
            val intent = Intent(this, Profile::class.java)
            startActivity(intent)
        }

        notificationsCard.setOnClickListener {

            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            startActivity(intent)
        }

        cameraPermissionCard.setOnClickListener {
            openAppSettings()
        }

        aboutUsCard.setOnClickListener {
            val intent = Intent(this, AboutUs::class.java)
            startActivity(intent)
        }

        privacyPolicyCard.setOnClickListener{
            val intent = Intent(this, PrivacyPolicy::class.java)
            startActivity(intent)
        }

        logoutCard.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    private fun saveThemeState(isNightMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isNightMode", isNightMode)
        editor.apply()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
        Toast.makeText(this, "Go to Permissions to enable the required permissions", Toast.LENGTH_LONG).show()
    }

    private fun isSystemInNightMode(): Boolean {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

}