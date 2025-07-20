package com.example.myapplication

data class User(
    val id: Int = 0, // Auto-incremented in DB, 0 for new user
    val username: String,
    val email: String,
    val password: String // This should match COL_PASSWORD in DatabaseHandler
)