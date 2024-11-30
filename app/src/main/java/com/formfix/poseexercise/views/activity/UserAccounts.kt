package com.formfix.poseexercise

data class UserAccounts(
    var userName: String = "",
    var userEmail: String = "",
    val isActive: Boolean = true
) {
    constructor() : this("","", true)
}