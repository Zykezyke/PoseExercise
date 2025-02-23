package com.formfix.poseexercise.data.results

data class RecentActivityItem(
    val imageResId: Int,
    val exerciseType: String,
    val reps: String,
    val date: String,
    val duration: String
)
