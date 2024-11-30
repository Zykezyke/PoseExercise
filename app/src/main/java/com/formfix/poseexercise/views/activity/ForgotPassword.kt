package com.formfix.poseexercise

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class ForgotPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var passwordRequirements: String
    private var email: String = ""
    private var validateObject = ValidationClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("UserAccounts")

        email = intent.getStringExtra("email") ?: ""

        passwordEditText = findViewById(R.id.etNewPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        val btnChangePassword: Button = findViewById(R.id.btnChangePassword)
        val ivTogglePassword: ImageView = findViewById(R.id.ivTogglePassword)
        val ivToggleConfirmPassword: ImageView = findViewById(R.id.ivToggleConfirmPassword)

        setupPasswordToggle(passwordEditText, ivTogglePassword)
        setupPasswordToggle(confirmPasswordEditText, ivToggleConfirmPassword)

        btnChangePassword.setOnClickListener {
            val newPassword = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            when {
                newPassword.isEmpty() -> showToast("Please enter new password")
                confirmPassword.isEmpty() -> showToast("Please confirm your password")
                newPassword != confirmPassword -> showToast("Passwords do not match")
                !validateObject.ValidatePassword(newPassword) -> showPasswordRequirementsDialog()
                else -> changePassword(newPassword)
            }
        }
    }

    private fun showPasswordRequirementsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Password Requirements")
            .setMessage("""
                Password must contain:
                • At least 8 characters
                • At least one uppercase letter
                • At least one lowercase letter
                • At least one number
                • At least one special character
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupPasswordToggle(editText: EditText, toggleButton: ImageView) {
        toggleButton.setOnClickListener {
            if (editText.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_visibility)
            } else {
                editText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleButton.setImageResource(R.drawable.ic_visible_off)
            }
            editText.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
            editText.setSelection(editText.text.length)
        }
    }

    private fun changePassword(newPassword: String) {
        if (!validateObject.ValidatePassword(newPassword)) {  // Changed to camelCase
            showPasswordRequirementsDialog()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset email sent successfully")
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    showToast("Failed to send password reset email")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

