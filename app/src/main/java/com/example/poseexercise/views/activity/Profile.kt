package com.example.poseexercise

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Profile : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val statisticsButton = findViewById<Button>(R.id.btnStat)
        val editButton = findViewById<Button>(R.id.btnEdit)
        val deactButton = findViewById<Button>(R.id.btnDeact)

        val emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddress2)
        val nameEditText = findViewById<EditText>(R.id.editTextText2)


        FirebaseAuth.getInstance().currentUser?.let { user ->
            emailEditText.setText(user.email)

            val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
            database.getReference("UserAccounts").child(user.uid).child("userName").get()
                .addOnSuccessListener { snapshot ->
                    val userName = snapshot.value as? String
                    nameEditText.setText(userName)
                }
        }

        editButton.setOnClickListener {
            showEditOptionsDialog()
        }


        backButton.setOnClickListener {
            finish()
        }

        deactButton.setOnClickListener{
            showDeactivateAccountDialog()
        }

        statisticsButton.setOnClickListener {
            val intent = Intent(this, ProfStatistics::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showEditOptionsDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_options)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val backButton = dialog.findViewById<ImageView>(R.id.backBtnOptions)
        val editUsernameButton = dialog.findViewById<Button>(R.id.btnEditUsername)
        val changePasswordButton = dialog.findViewById<Button>(R.id.btnChangePassword)

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        editUsernameButton.setOnClickListener {
            dialog.dismiss()
            showEditUsernameDialog()
        }

        changePasswordButton.setOnClickListener {
            dialog.dismiss()
            showEditPasswordDialog()
        }

        // Set the dialog width to 90% of screen width
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }

    private fun showEditUsernameDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_username)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Makes the dialog properly rounded

        val etNewUsername = dialog.findViewById<EditText>(R.id.etNewUsername)
        val backButton = dialog.findViewById<ImageView>(R.id.backBtnUser)
        val saveButton = dialog.findViewById<Button>(R.id.saveBtnUser)

        // Get current username
        val currentUsername = findViewById<EditText>(R.id.editTextText2).text.toString()
        etNewUsername.setText(currentUsername)

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val newUsername = etNewUsername.text.toString()
            if (newUsername.isNotEmpty()) {
                updateUsername(newUsername)
                dialog.dismiss()
            } else {
                showToast("Username cannot be empty")
            }
        }

        // Set the dialog width to 90% of screen width
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }

    private fun showEditPasswordDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_edit_password)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etCurrentPassword = dialog.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialog.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialog.findViewById<EditText>(R.id.etConfirmPassword)
        val backButton = dialog.findViewById<ImageView>(R.id.backBtnPass)
        val saveButton = dialog.findViewById<Button>(R.id.saveBtnPass)

        val ivToggleCurrentPassword = dialog.findViewById<ImageView>(R.id.ivToggleCurrentPassword)
        val ivToggleNewPassword = dialog.findViewById<ImageView>(R.id.ivToggleNewPassword)
        val ivToggleConfirmPassword = dialog.findViewById<ImageView>(R.id.ivToggleConfirmPassword)


        ivToggleCurrentPassword.setOnClickListener {
            if (etCurrentPassword.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                etCurrentPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleCurrentPassword.setImageResource(R.drawable.ic_visibility)
            } else {

                etCurrentPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleCurrentPassword.setImageResource(R.drawable.ic_visible_off)
            }
            etCurrentPassword.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
            etCurrentPassword.setSelection(etCurrentPassword.text.length)
        }


        ivToggleNewPassword.setOnClickListener {
            if (etNewPassword.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                etNewPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleNewPassword.setImageResource(R.drawable.ic_visibility)
            } else {

                etNewPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleNewPassword.setImageResource(R.drawable.ic_visible_off)
            }
            etNewPassword.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
            etNewPassword.setSelection(etNewPassword.text.length)
        }


        ivToggleConfirmPassword.setOnClickListener {
            if (etConfirmPassword.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {

                etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
            } else {

                etConfirmPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivToggleConfirmPassword.setImageResource(R.drawable.ic_visible_off)
            }
            etConfirmPassword.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
            etConfirmPassword.setSelection(etConfirmPassword.text.length)
        }

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            when {
                currentPassword.isEmpty() -> {
                    showToast("Please enter your current password")
                }
                !isValidPassword(newPassword) -> {
                    showPasswordRequirementsDialog()
                }
                newPassword != confirmPassword -> {
                    showToast("New passwords do not match")
                }
                else -> {
                    updatePassword(currentPassword, newPassword) {
                        if (it) dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
        // Make the dialog fill the width of the screen while maintaining wrap content for height
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, android.view.WindowManager.LayoutParams.WRAP_CONTENT)
    }

    private fun updateUsername(newUsername: String) {
        val user = auth.currentUser
        if (user?.email == null) return

        val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
        database.getReference("UserAccounts").child(user.uid).child("userName")
            .setValue(newUsername)
            .addOnSuccessListener {
                findViewById<EditText>(R.id.editTextText2).setText(newUsername)
                showToast("Username updated successfully")
            }
            .addOnFailureListener {
                showToast("Failed to update username")
            }
    }

    private fun updatePassword(currentPassword: String, newPassword: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user?.email == null) return

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        showToast("Password updated successfully")
                        onComplete(true)
                    }
                    .addOnFailureListener {
                        showToast("Failed to update password")
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                showToast("Current password is incorrect")
                onComplete(false)
            }
    }

    private fun isValidPassword(password: String): Boolean {
        return ValidationClass().ValidatePassword(password)
    }

    private fun showPasswordRequirementsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Password Requirements")
            .setMessage("""
                Password must:
                • Be 6-12 characters long
                • Contain at least 1 uppercase letter
                • Contain at least 1 lowercase letter
                • Contain at least 1 number
                • Contain at least 1 special character
            """.trimIndent())
            .setPositiveButton("Got it") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showDeactivateAccountDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_deactivate_account)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etPassword = dialog.findViewById<EditText>(R.id.etPassword)
        val confirmButton = dialog.findViewById<Button>(R.id.confirmButton)
        val backButton = dialog.findViewById<ImageView>(R.id.backBtnDeactivate)

        backButton.setOnClickListener {
            dialog.dismiss()
        }

        confirmButton.setOnClickListener {
            val password = etPassword.text.toString()
            if (password.isEmpty()) {
                showToast("Please enter your password")
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user?.email == null) {
                showToast("Unable to deactivate account")
                return@setOnClickListener
            }

            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Disable the user account
                    val database = FirebaseDatabase.getInstance("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    database.getReference("UserAccounts").child(user.uid)
                        .child("active")
                        .setValue(false)
                        .addOnSuccessListener {
                            showToast("Account successfully deactivated")
                            auth.signOut() // Sign out the user
                            dialog.dismiss()
                            // Navigate to login screen
                            val intent = Intent(this, Login::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to deactivate account: ${e.message}")
                        }
                }
                .addOnFailureListener {
                    showToast("Incorrect password")
                }
        }

        // Set the dialog width to 90% of screen width
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}