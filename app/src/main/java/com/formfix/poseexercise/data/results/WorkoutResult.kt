package com.formfix.poseexercise.data.results

import androidx.room.TypeConverter
import java.util.Date


data class WorkoutResult(

    val id: Int = 0,
    val exerciseName: String = "",
    val repeatedCount: Int = 0,
    var confidence: Float = 0f,

    val timestamp: Long = 0,
    val calorie: Double = 0.0,
    val workoutTimeInMin: Double = 0.0
    )


// To convert Long value to Date and vice versa
class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}