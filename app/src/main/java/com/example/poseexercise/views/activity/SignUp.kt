package com.example.poseexercise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        var validateObject = ValidationClass()

        database = Firebase.database("https://formfix-221ea-default-rtdb.asia-southeast1.firebasedatabase.app//").getReference("UserAccounts")
        auth = FirebaseAuth.getInstance()


        emailEditText = findViewById(R.id.etEmail)
        nameEditText = findViewById(R.id.etName)
        passwordEditText = findViewById(R.id.etPassword)
        val signUpButton: Button = findViewById(R.id.btnSignUp)

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val name = nameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty()) {
                if (validateObject.ValidatePassword(password)) {
                    registerUser(name, email, password)
                } else {
                    showToast(this, "Please enter a valid password.")
                }
            } else {
                showToast(this, "Please fill all fields")
            }
        }
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
        val newUser = UserAccounts(name, email) // Customize this based on your data model

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

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
