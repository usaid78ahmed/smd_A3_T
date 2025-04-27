package com.amnabatool.assignment_2

data class Story(
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val storyImageUrl: String = "",
    val timestamp: Long = 0,
    val hasViewed: Boolean = false
)