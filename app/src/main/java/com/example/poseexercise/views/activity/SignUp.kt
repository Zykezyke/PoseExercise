package com.example.poseexercise

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.util.Random
import java.util.regex.Pattern
import android.text.Html
import android.widget.ImageButton

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordRequirements: String
    private lateinit var termsTextView: TextView
    private lateinit var checkboxTerms: CheckBox
    private var otpTimestamp: Long = 0
    private val OTP_VALIDITY_PERIOD = 5 * 60 * 1000
    private var generatedOTP: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var validateObject = ValidationClass()

        database = Firebase.database("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("UserAccounts")
        auth = FirebaseAuth.getInstance()


        emailEditText = findViewById(R.id.etEmail)
        nameEditText = findViewById(R.id.etName)
        passwordEditText = findViewById(R.id.etPassword)
        termsTextView = findViewById(R.id.termsTextView)
        checkboxTerms = findViewById(R.id.checkboxTerms)

        val signUpButton: Button = findViewById(R.id.btnSignUp)
        val loginLink: TextView = findViewById(R.id.LoginHere)
        val ivTogglePassword: ImageView = findViewById(R.id.ivTogglePassword)
        val ivInfoPassword: ImageView = findViewById(R.id.ivInfo)

        loginLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        termsTextView.paintFlags =  Paint.UNDERLINE_TEXT_FLAG
        checkboxTerms.isEnabled = false

        updatePasswordRequirements(passwordEditText.text.toString())

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePasswordRequirements(s.toString())
            }
        })

        termsTextView.setOnClickListener {
            showTermsAndConditionsDialog()
        }

        loginLink.setOnClickListener {
            // Navigate to the login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        ivInfoPassword.setOnClickListener {
            showPasswordRequirementsDialog()
        }

        ivTogglePassword.setOnClickListener {
            val currentInputType = passwordEditText.inputType
            if (currentInputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // Show password
                passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visibility)
            } else {
                // Hide password
                passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                ivTogglePassword.setImageResource(R.drawable.ic_visible_off)
            }

            passwordEditText.typeface = android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL)
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val name = nameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() && name.isEmpty() && password.isEmpty()) {
                showToast(this, "Please fill all fields")
            } else if (email.isEmpty()) {
                showToast(this, "Email cannot be empty")
            } else if (!validateObject.isValidEmail(email)) {
                showToast(this, "Please enter a valid email address")
            } else if (name.isEmpty()) {
                showToast(this, "Name cannot be empty")
            } else if (password.isEmpty()) {
                showToast(this, "Password cannot be empty")
            } else if (!checkboxTerms.isChecked) {
                showToast(this, "Please accept the terms and conditions")
            } else {
                if (validateObject.ValidatePassword(password)) {
                    // Check if the email exists before sending OTP
                    checkIfEmailExists(email, name, password)
                } else {
                    showPasswordRequirementsDialog()
                }
            }
        }
    }

    private fun checkIfEmailExists(email: String, name: String, password: String) {
        val query = database.orderByChild("userEmail").equalTo(email)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Email already exists
                    showToast(this@SignUp, "This email is already registered. Please use another one or log in.")
                } else {
                    // Email is not registered, proceed with OTP
                    sendOTP(email, name, password)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast(this@SignUp, "Failed to check email. Please try again.")
                Log.e("Error_FormFix", error.message)
            }
        })
    }


    private fun sendOTP(email: String, name: String, password: String) {
        // Show loading dialog
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Sending OTP")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        // Generate OTP
        generatedOTP = Random().nextInt(999999).toString().padStart(6, '0')
        otpTimestamp = System.currentTimeMillis()

        // Send OTP via email
        EmailSender.sendOTPEmail(email, generatedOTP, name) { success ->
            runOnUiThread {
                loadingDialog.dismiss()
                if (success) {
                    showToast(this, "OTP sent successfully!")
                    showOTPVerificationDialog(email, name, password)
                } else {
                    showToast(this, "Failed to send OTP. Please try again.")
                }
            }
        }
    }

    private fun showOTPVerificationDialog(email: String, name: String, password: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_verification, null)
        val otpEditText = dialogView.findViewById<EditText>(R.id.etOTP)
        val btnVerify = dialogView.findViewById<Button>(R.id.btnVerifyOTP)
        val btnResendOTP = dialogView.findViewById<Button>(R.id.btnResendOTP)
        val tvTimer = dialogView.findViewById<TextView>(R.id.tvTimer)
        val btnExit = dialogView.findViewById<ImageButton>(R.id.btnExit)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        startOTPTimer(tvTimer)

        btnExit.setOnClickListener{
            dialog.dismiss()
        }

        btnVerify.setOnClickListener {
            val enteredOTP = otpEditText.text.toString()

            // Check if OTP is expired
            if (System.currentTimeMillis() - otpTimestamp > OTP_VALIDITY_PERIOD) {
                showToast(this, "OTP has expired. Please request a new one.")
                return@setOnClickListener
            }

            if (enteredOTP == generatedOTP) {
                dialog.dismiss()
                registerUser(name, email, password)
            } else {
                showToast(this, "Invalid OTP. Please try again.")
            }
        }



        btnResendOTP.setOnClickListener {
            dialog.dismiss()
            sendOTP(email, name, password)
        }

        dialog.show()


    }

    private fun startOTPTimer(timerTextView: TextView) {
        object : CountDownTimer(OTP_VALIDITY_PERIOD.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                timerTextView.text = String.format("OTP expires in: %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "OTP expired. Please request a new one."
            }
        }.start()
    }


    private fun showTermsAndConditionsDialog() {
        val termsMessage = """
        Welcome to FormFix! By registering to our app, you agree to the following Terms and Conditions. Please read them carefully to understand how FormFix works and what responsibilities you have when using our app.<br><br>
        <b>1. Acceptance of Terms</b><br>
        By registering, accessing, or using the FormFix app, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use the app.<br><br>
        <b>2. Purpose of FormFix</b><br>
        FormFix is a workout assistant app designed to help users improve their exercise form by offering real-time corrections and feedback on their posture. The app records repetition counts and a percentage score reflecting form correction to aid in tracking workout performance. FormFix also includes a Body Mass Index (BMI) calculator, though it does not store or collect any data entered into this tool.<br><br>
        <b>3. Information We Collect</b><br>
        To register and use FormFix, you must provide the following information:<br><br>
        Email Address: Required for account setup and communication purposes.<br>
        Username: Used to personalize the app experience.<br>
        The app also collects workout data, specifically:<br><br>
        Repetition Counts: Recorded to help track user progress.<br>
        Correction Percentage: Indicates the quality of the exercise form.<br>
        No other personal information is collected.<br><br>
        <b>4. Data Usage</b><br>
        The information collected by FormFix is used solely to enhance your experience with the app. The collected data (reps and correction percentage) is used to generate a workout report for your review post-exercise. Data from the BMI calculator is not stored, collected, or used by FormFix.<br><br>
        <b>5. Privacy</b><br>
        FormFix respects your privacy and takes all reasonable measures to protect your data. Only minimal information (email and username) is required, and all workout data collected (reps and correction percentage) remains private within the app.<br><br>
        <b>6. Account Security</b><br>
        You are responsible for keeping your account secure. Please do not share your login credentials with others. If you suspect unauthorized access, please contact us immediately.<br><br>
        <b>7. Limitation of Liability</b><br>
        FormFix and its developers are not liable for any indirect, incidental, or consequential damages arising from your use of the app. This includes any issues with form correction feedback or reliance on data provided by FormFix.<br><br>
        <b>8. Modification of Terms</b><br>
        FormFix reserves the right to modify these Terms and Conditions at any time. Users will be notified of significant changes.<br><br>
        <b>9. Termination of Use</b><br>
        We reserve the right to suspend or terminate your access to FormFix at any time, particularly if these Terms and Conditions are violated.<br><br>
        <b>10. Contact Us</b><br>
        For questions or concerns regarding these Terms and Conditions, please contact us at [support email address].<br><br>
        By using FormFix, you acknowledge that you have read, understood, and agreed to these Terms and Conditions. Thank you for choosing FormFix to improve your workouts!
    """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("Terms and Conditions")
            .setMessage(Html.fromHtml(termsMessage))
            .setPositiveButton("Accept") { dialog, _ ->
                checkboxTerms.isChecked = true
                dialog.dismiss()
            }
            .setNegativeButton("Decline") { dialog, _ ->
                checkboxTerms.isChecked = false
                dialog.dismiss()
            }
            .show()
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showToast(this, "Registration successful!")

                    // Get the UID of the newly registered user
                    val userUid = auth.currentUser?.uid

                    // Pass the UID to the function that handles user registration
                    userUid?.let { uid ->
                        handleUserRegistration(uid, name, email)
                    }
                } else {
                    showToast(this, "Registration failed. Please try again.")
                    Log.e("Error_FormFix", task.exception?.message.toString())
                }
            }
    }

    private fun handleUserRegistration(uid: String, name: String, email: String) {
        val userReference = database.child(uid)

        // Create a UserAccounts object with the necessary data
        val newUser = UserAccounts(name, email, isActive = true) // Customize this based on your data model

        // Set the user data in the Realtime Database
        userReference.setValue(newUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(this, "User data saved successfully!")

                    // Redirect to the main activity or another screen
                    startActivity(Intent(this, Login::class.java))
                    finish()
                } else {
                    showToast(this, "Failed to save user data.")
                    Log.e("Error_FormFix", task.exception?.message.toString())
                }
            }
    }

    private fun showPasswordRequirementsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Password Requirements")
            .setMessage(passwordRequirements)
            .setPositiveButton("Got it") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updatePasswordRequirements(password: String) {
        val validateObject = ValidationClass()
        val requirements = StringBuilder()
        requirements.append("Password must:\n")

        if (password.length < 6 || password.length > 12) {
            requirements.append("• Be 6-12 characters long ❌\n")
        } else {
            requirements.append("• Be 6-12 characters long ✓\n")
        }

        if (!password.any { it.isUpperCase() }) {
            requirements.append("• Contain at least 1 uppercase letter ❌\n")
        } else {
            requirements.append("• Contain at least 1 uppercase letter ✓\n")
        }

        if (!password.any { it.isLowerCase() }) {
            requirements.append("• Contain at least 1 lowercase letter ❌\n")
        } else {
            requirements.append("• Contain at least 1 lowercase letter ✓\n")
        }

        if (!password.any { it.isDigit() }) {
            requirements.append("• Contain at least 1 number ❌\n")
        } else {
            requirements.append("• Contain at least 1 number ✓\n")
        }

        if (!validateObject.containsAllowedSpecialChar(password)) {
            requirements.append("• Contain at least 1 special character (^$*.[]{}()?\"!@#%&/\\,><':;|_~) ❌")
        } else {
            requirements.append("• Contain at least 1 special character (^$*.[]{}()?\"!@#%&/\\,><':;|_~) ✓")
        }

        passwordRequirements = requirements.toString()
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}