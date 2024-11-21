package com.example.poseexercise

import com.example.poseexercise.BuildConfig
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

class BmiCalculator : AppCompatActivity() {
    private lateinit var unitToggle: MaterialButtonToggleGroup
    private lateinit var genderToggle: MaterialButtonToggleGroup
    private lateinit var weightLayout: TextInputLayout
    private lateinit var heightLayout: TextInputLayout
    private lateinit var weightEdit: EditText
    private lateinit var heightEdit: EditText
    private lateinit var ageEdit: EditText
    private lateinit var calculateBtn: Button
    private lateinit var bmiResultText: TextView
    private lateinit var categoryText: TextView
    private lateinit var messageText: TextView
    private lateinit var genderAgeText: TextView
    private lateinit var backButton: ImageView
    private lateinit var generativeModel: GenerativeModel


    private var isMetric = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bmi_calculator)

        initializeViews()
        setupListeners()

        generativeModel = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        unitToggle.check(R.id.metricButton)
        genderToggle.check(R.id.maleButton)
        updateUnitHints()
        bmiResultText.background = null
    }

    private fun setupListeners() {
        unitToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isMetric = checkedId == R.id.metricButton
                updateUnitHints()
            }
        }

        calculateBtn.setOnClickListener {
            calculateBMI()
        }


        backButton.setOnClickListener{
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initializeViews() {
        unitToggle = findViewById(R.id.unitToggle)
        genderToggle = findViewById(R.id.genderToggle)
        weightLayout = findViewById(R.id.weightLayout)
        heightLayout = findViewById(R.id.heightLayout)
        weightEdit = findViewById(R.id.weightEdit)
        heightEdit = findViewById(R.id.heightEdit)
        ageEdit = findViewById(R.id.ageEdit)
        calculateBtn = findViewById(R.id.calculateBtn)
        bmiResultText = findViewById(R.id.bmiResultText)
        categoryText = findViewById(R.id.categoryText)
        messageText = findViewById(R.id.messageText)
        genderAgeText = findViewById(R.id.genderAgeText)
        backButton = findViewById(R.id.btnBack)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (heightEdit.text.isNullOrEmpty() && weightEdit.text.isNullOrEmpty() && ageEdit.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        else if (weightEdit.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        else if (heightEdit.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter height", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        else if (ageEdit.text.isNullOrEmpty()) {
            Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    private suspend fun generateGeminiMessage(bmi: Float, age: Int, isMale: Boolean, category: String): String {
        val prompt = """
            Act as a health advisor. Generate a short, personalized health message (maximum 3 sentences) for a person with the following characteristics:
            - BMI: $bmi (Category: $category)
            - Age: $age
            - Gender: ${if (isMale) "Male" else "Female"}
            
            The message should be encouraging, informative, and provide specific, actionable advice based on their BMI category and age group. Do not create Dear [name]
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.trim() ?: fallbackMessage(bmi, category)
        } catch (e: Exception) {
            fallbackMessage(bmi, category)
        }
    }

    private fun fallbackMessage(bmi: Float, category: String): String {
        return "Based on your BMI of $bmi, you're in the $category category. Consider consulting with a healthcare provider for personalized advice on maintaining a healthy lifestyle."
    }

    private fun updateUnitHints() {
        if (isMetric) {
            weightLayout.hint = "Weight (kg)"
            heightLayout.hint = "Height (cm)"
        } else {
            weightLayout.hint = "Weight (lbs)"
            heightLayout.hint = "Height (in)"
        }
    }

    private fun calculateBMI() {
        if (!validateInputs()) {
            bmiResultText.text = ""
            bmiResultText.background = null
            categoryText.text = ""
            genderAgeText.visibility = View.GONE
            messageText.visibility = View.GONE
            return
        }

        val weight = weightEdit.text.toString().toFloatOrNull()
        val height = heightEdit.text.toString().toFloatOrNull()
        val age = ageEdit.text.toString().toIntOrNull()
        val isMale = genderToggle.checkedButtonId == R.id.maleButton

        if (weight != null && height != null && age != null) {
            val bmi = if (isMetric) {
                weight / (height / 100).pow(2)
            } else {
                703 * (weight / height.pow(2))
            }
            val bmiResult = String.format("%.1f", bmi)

            val (category, color) = when {
                bmi < 18.5 -> Pair("Underweight", ContextCompat.getColor(this, R.color.underweight))
                bmi < 25 -> Pair("Normal weight", ContextCompat.getColor(this, R.color.normal))
                bmi < 30 -> Pair("Overweight", ContextCompat.getColor(this, R.color.overweight))
                else -> Pair("Obese", ContextCompat.getColor(this, R.color.obese))
            }

            messageText.text = "Generating personalized advice..."

            CoroutineScope(Dispatchers.Main).launch {
                val aiMessage = generateGeminiMessage(bmi, age, isMale, category)
                messageText.text = aiMessage
            }
            bmiResultText.background = null

            val drawable = ContextCompat.getDrawable(this, R.drawable.pill_background) as? GradientDrawable
            drawable?.setColor(color)
            bmiResultText.background = drawable

            bmiResultText.text = bmiResult
            categoryText.text = category
            categoryText.setTextColor(color)

            val genderText = if (isMale) "Male" else "Female"
            genderAgeText.text = "Gender: $genderText, Age: $age"
            genderAgeText.visibility = View.VISIBLE
            messageText.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bmiResultText.background = null
    }

    override fun onPause() {
        super.onPause()
        bmiResultText.background = null
    }


}