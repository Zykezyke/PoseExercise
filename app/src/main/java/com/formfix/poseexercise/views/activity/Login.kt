package com.formfix.poseexercise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import java.security.SecureRandom

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var ivTogglePassword: ImageView
    private lateinit var forgotPasswordText : TextView
    private var otpTimestamp: Long = 0
    private val OTP_VALIDITY_PERIOD = 5 * 60 * 1000
    private var resendCount = 0
    private val MAX_RESEND_ATTEMPTS = 3
    val secureRandom = SecureRandom()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = Firebase.database("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("UserAccounts")
        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.etUsername)
        passwordEditText = findViewById(R.id.etPassword)
        val loginButton: Button = findViewById(R.id.btnLogin)
        val signUpTextView: TextView = findViewById(R.id.tvSignUp)
        ivTogglePassword = findViewById(R.id.ivTogglePassword)
        forgotPasswordText = findViewById(R.id.textView)

        signUpTextView.paintFlags = signUpTextView.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, Home::class.java))
            finish()
            return
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Password is empty", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }


        }

        signUpTextView.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        forgotPasswordText.setOnClickListener{
            showForgotPasswordDialog()
        }

        ivTogglePassword.setOnClickListener {
            if (passwordEditText.inputType == (android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
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
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the UID of the authenticated user
                    val userUid = auth.currentUser?.uid

                    // Check if user is active before proceeding
                    userUid?.let { uid ->
                        val userReference = database.child(uid)
                        userReference.child("active").get().addOnCompleteListener { activeTask ->
                            if (activeTask.isSuccessful) {
                                val isActive = activeTask.result.getValue(Boolean::class.java) ?: false
                                if (isActive) {
                                    // User is active, proceed with login
                                    handleUserAuthentication(uid)
                                } else {
                                    // User is not active, sign them out and show message
                                    auth.signOut()
                                    showReactivationDialog(email)
                                }
                            } else {
                                // Error checking active status
                                auth.signOut()
                                showToast(applicationContext, "Error verifying account status. Please try again.")
                                Log.e("Error_FormFix", "Error checking active status: ${activeTask.exception?.message}")
                            }
                        }
                    }
                } else {
                    showToast(applicationContext, "Authentication Failed")
                }
            }
    }

    private fun handleUserAuthentication(uid: String) {
        val userReference = database.child(uid)

        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserAccounts::class.java)
                    if (user != null) {
                        showToast(applicationContext, "Welcome back, ${user.userEmail}!")
                    }
                } else {
                    showToast(applicationContext, "New user! Welcome!")
                }

                startActivity(Intent(this@Login, Home::class.java))
                finish()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Error_FormFix", "Error checking user existence: ${error.message}")
            }
        })
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val emailInput = dialogView.findViewById<EditText>(R.id.etEmailForgot)

        MaterialAlertDialogBuilder(this)
            .setTitle("Forgot Password")
            .setView(dialogView)
            .setPositiveButton("Submit") { dialog, _ ->
                val email = emailInput.text.toString()
                if (email.isNotEmpty()) {
                    checkEmailAndSendOTP(email)
                } else {
                    showToast(this, "Please enter your email")
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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



    private fun checkEmailAndSendOTP(email: String) {
        database.orderByChild("userEmail").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.children.first().getValue(UserAccounts::class.java)
                        user?.let {
                            sendOTP(email, it.userName)
                        }
                    } else {
                        showToast(this@Login, "Email not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast(this@Login, "Error checking email")
                }
            })
    }

    @SuppressLint("DefaultLocale")
    private fun sendOTP(email: String, name: String) {
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Sending OTP")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        val otp = String.format("%06d", secureRandom.nextInt(1000000))
        otpTimestamp = System.currentTimeMillis()

        EmailSender.sendForgotEmail(email, otp, name) { success ->
            runOnUiThread {
                loadingDialog.dismiss()
                if (success) {
                    showToast(this, "OTP sent successfully!")
                    showOTPVerificationDialog(email, otp)
                } else {
                    showToast(this, "Failed to send OTP")
                }
            }
        }
    }

    private fun showOTPVerificationDialog(email: String, otp: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verification, null)
        val otpEditText = dialogView.findViewById<EditText>(R.id.etOTP)
        val btnVerify = dialogView.findViewById<Button>(R.id.btnVerifyOTP)
        val btnResendOTP = dialogView.findViewById<Button>(R.id.btnResendOTP)
        val tvTimer = dialogView.findViewById<TextView>(R.id.tvTimer)
        val btnExit = dialogView.findViewById<ImageButton>(R.id.btnExit)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnExit.setOnClickListener {
            dialog.dismiss()
        }

        startOTPTimer(tvTimer)

        btnVerify.setOnClickListener {
            if (System.currentTimeMillis() - otpTimestamp > OTP_VALIDITY_PERIOD) {
                showToast(this, "OTP has expired. Please request a new one.")
                return@setOnClickListener
            }

            if (otpEditText.text.toString() == otp) {
                dialog.dismiss()
                changePassword(email)
            } else {
                showToast(this, "Invalid OTP")
            }
        }

        btnResendOTP.setOnClickListener {
            if (resendCount >= MAX_RESEND_ATTEMPTS) {
                showToast(this, "Resent many times. Please try again later.")
                return@setOnClickListener
            }
            resendCount++
            dialog.dismiss()
            checkEmailAndSendOTP(email)
        }

        dialog.show()
    }

    private fun changePassword(email:String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast(this, "Password reset email sent successfully")
                } else {
                    showToast(this,"Failed to send password reset email")
                }
            }
    }

    private fun showReactivationDialog(email: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Account Deactivated")
            .setMessage("Your account is currently deactivated. Would you like to reactivate it?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                checkEmailAndSendReactivationOTP(email)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun checkEmailAndSendReactivationOTP(email: String) {
        database.orderByChild("userEmail").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userSnapshot = snapshot.children.first()
                        val user = userSnapshot.getValue(UserAccounts::class.java)
                        val userId = userSnapshot.key
                        user?.let {
                            sendReactivationOTP(email, it.userName, userId ?: "")
                        }
                    } else {
                        showToast(this@Login, "Email not found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast(this@Login, "Error checking email")
                }
            })
    }

    private fun sendReactivationOTP(email: String, name: String, userId: String) {
        val loadingDialog = MaterialAlertDialogBuilder(this)
            .setTitle("Sending OTP")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        loadingDialog.show()

        val otp = String.format("%06d", secureRandom.nextInt(1000000))
        otpTimestamp = System.currentTimeMillis()

        EmailSender.sendReactivationEmail(email, otp, name) { success ->
            runOnUiThread {
                loadingDialog.dismiss()
                if (success) {
                    showToast(this, "OTP sent successfully!")
                    showReactivationOTPDialog(email, otp, userId)
                } else {
                    showToast(this, "Failed to send OTP")
                }
            }
        }
    }

    private fun showReactivationOTPDialog(email: String, otp: String, userId: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verification, null)
        val otpEditText = dialogView.findViewById<EditText>(R.id.etOTP)
        val btnVerify = dialogView.findViewById<Button>(R.id.btnVerifyOTP)
        val btnResendOTP = dialogView.findViewById<Button>(R.id.btnResendOTP)
        val tvTimer = dialogView.findViewById<TextView>(R.id.tvTimer)
        val btnExit = dialogView.findViewById<ImageButton>(R.id.btnExit)

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnExit.setOnClickListener {
            dialog.dismiss()
        }

        startOTPTimer(tvTimer)

        btnVerify.setOnClickListener {
            if (System.currentTimeMillis() - otpTimestamp > OTP_VALIDITY_PERIOD) {
                showToast(this, "OTP has expired. Please request a new one.")
                return@setOnClickListener
            }

            if (otpEditText.text.toString() == otp) {
                dialog.dismiss()
                reactivateAccount(userId)
            } else {
                showToast(this, "Invalid OTP")
            }
        }

        btnResendOTP.setOnClickListener {
            if (resendCount >= MAX_RESEND_ATTEMPTS) {
                showToast(this, "Maximum resend attempts reached. Please try again later.")
                return@setOnClickListener
            }
            resendCount++
            dialog.dismiss()
            checkEmailAndSendReactivationOTP(email)
        }

        dialog.show()
    }

    private fun reactivateAccount(userId: String) {
        val userReference = database.child(userId)
        userReference.child("active").setValue(true)
            .addOnSuccessListener {
                showToast(this, "Account reactivated successfully! Please login again.")
            }
            .addOnFailureListener {
                showToast(this, "Failed to reactivate account. Please try again.")
                Log.e("Error_FormFix", "Error reactivating account: ${it.message}")
            }
    }
}