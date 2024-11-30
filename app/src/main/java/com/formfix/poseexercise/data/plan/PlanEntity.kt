package com.formfix.poseexercise.data.plan

data class Plan(
    var id: String = "",
    var exercise: String = "",
    var calories: Double = 0.0,
    var repeatCount: Int = 0,
    var selectedDays: String = "",
    var completed: Boolean = false,
    var timeCompleted: Long? = null
) {
    constructor() : this("", "", 0.0, 0, "", false, null)
}