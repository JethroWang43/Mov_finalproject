package com.example.myapplication

data class User(
    val username: String,
    val email: String,
    val password: String,
    val id: Int = 0 // Default value for id, as it's AUTOINCREMENT in DB
)