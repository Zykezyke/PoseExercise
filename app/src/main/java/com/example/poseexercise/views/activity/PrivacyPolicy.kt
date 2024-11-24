package com.example.poseexercise

import android.os.Bundle
import com.example.poseexercise.R
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.text.util.Linkify
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PrivacyPolicy : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_privacy_policy)

        val backButton = findViewById<ImageView>(R.id.btnBack)
        val privacyPolicyContent = findViewById<TextView>(R.id.privacyPolicyContent)

        backButton.setOnClickListener {
            finish()
        }

        val content = buildPrivacyPolicy()
        privacyPolicyContent.text = content

        // Enable link detection
        Linkify.addLinks(privacyPolicyContent, Linkify.WEB_URLS)

        // Set TextView to handle clickable links
        privacyPolicyContent.movementMethod = android.text.method.LinkMovementMethod.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun buildPrivacyPolicy(): SpannableStringBuilder {
        val builder = SpannableStringBuilder()

        // Helper function to add sections
        fun addSection(title: String, content: String) {
            val titleSpan = SpannableString("$title\n\n")
            titleSpan.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)
            titleSpan.setSpan(RelativeSizeSpan(1.2f), 0, title.length, 0)

            builder.append(titleSpan)
            builder.append("$content\n\n")
        }

        // Welcome Section
        builder.append("Welcome to FormFix's Privacy Policy. This document explains how we collect, use, and protect your information while using our exercise form improvement app.\n\n")

        // Information Collection
        addSection("1. Information We Collect",
            """At FormFix, we believe in minimal data collection. We only gather what's necessary to provide you with accurate form correction and progress tracking:

Required Account Information:
• Email Address: Used for account creation and essential communications
• Username: For personalizing your app experience

Workout Performance Data:
• Repetition Counts: To track your exercise progress
• Form Correction Percentages: To measure and improve your exercise technique

Additional Notes:
• Our BMI calculator is available as a tool, but we do not store or collect any data entered into it
• We do not collect any payment information
• No location data is gathered
• Camera access is only used during workouts for real-time form analysis""")

        // Data Usage
        addSection("2. How We Use Your Information",
            """Your data is used exclusively to enhance your workout experience:

• Provide real-time form correction and feedback during exercises
• Generate detailed post-workout reports showing your progress
• Track your improvement over time through repetition counts and form scores
• Send important app updates and account-related notifications

We prioritize transparency and will never:
• Sell your personal information
• Share your workout data with third parties
• Use your information for marketing purposes without consent
• Track your activity outside of workout sessions""")

        // Data Protection
        addSection("3. Data Protection",
            """Your privacy and data security are our top priorities:

• Encrypted Storage: All personal data is encrypted
• Secure Access: Only you can access your workout history and performance metrics
• Local Processing: Form analysis is performed locally on your device
• Regular Updates: We continuously update our security measures
• Data Minimization: We only store essential information needed for app functionality""")

        // Account Security
        addSection("4. User Account Security",
            """To maintain the security of your account:

• Create a strong, unique password
• Never share your login credentials
• Log out when using shared devices
• Contact us immediately if you suspect unauthorized access""")

        // User Rights
        addSection("5. Your Privacy Rights",
            """You have full control over your data:

• Access your stored information at any time
• Request data deletion
• Opt-out of non-essential communications
• Export your workout history
• Update your personal information

To exercise these rights, contact our support team through the app or via email.""")

        // Contact Information
        addSection("Contact Us",
            """Questions about your privacy? We're here to help:

Email: formfixteam@gmail.com
Facebook: https://www.facebook.com/profile.php?id=61569363091691
Twitter: https://x.com/form_fix
Instagram: https://www.instagram.com/formfixteam/

We aim to respond to all privacy-related inquiries within 48 hours.""")

        return builder
    }
}