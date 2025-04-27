package com.amnabatool.assignment_2

data class Contact(
    val name: String,
    val email: String, // Added for user identification
    val imageResId: Int,
    val userId: String // Added for Firebase operations
)