package com.formfix.poseexercise

import android.util.Log
import javax.mail.Session
import java.util.*

class ValidationClass {

    public fun containsAllowedSpecialChar(password: String): Boolean {
        val allowedSpecialChars = setOf(
            '^', '$', '*', '.', '[', ']', '{', '}',
            '(', ')', '?', '"', '!', '@', '#', '%',
            '&', '/', '\\', ',', '>', '<', '\'',
            ':', ';', '|', '_', '~'
        )
        return password.any { it in allowedSpecialChars }
    }

    public fun ValidatePassword(password: String): Boolean {
        try {
            // Check if the password length is between 5 and 12 characters
            if (password.length >= 6 && password.length <= 12) {
                if (!password.isNullOrBlank()) {
                    val char_of_string = password.toCharArray()

                    val (uppercase, notUppercase) = char_of_string.partition { it.isUpperCase() }
                    if (uppercase.isNotEmpty()) {

                        val (lowercase, notLowercase) = char_of_string.partition { it.isLowerCase() }
                        if (lowercase.isNotEmpty()) {

                            val (isNumber, isNotNumber) = char_of_string.partition { it.isDigit() }
                            if (isNumber.isNotEmpty()) {

                                if (containsAllowedSpecialChar(password)) {
                                    return true
                                }
                            }
                        }
                    }
                }
            }
            return false
        } catch (e: Exception) {
            Log.e("error_formfix", e.message.toString())
            return false
        }
    }

    public fun isValidEmail(email: String): Boolean {
        val session = Session.getDefaultInstance(Properties())
        return try {
            val address = javax.mail.internet.InternetAddress(email)
            address.validate()
            true
        } catch (ex: Exception) {
            false
        }
    }
}