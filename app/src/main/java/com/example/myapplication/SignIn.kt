package com.example.myapplication

import android.content.Context // Import Context for SharedPreferences
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var db: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHandler(this)

        binding.registerText.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        binding.forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {
            val usernameOrEmail = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter username/email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- IMPORTANT CHANGE: Get the actual username/email for storage ---
            // First, check credentials. If valid, retrieve the actual username and email
            // (since the user might have logged in with email, but we want the username too)
            val userDetails = db.getUserDetails(usernameOrEmail, password) // New DB function needed!

            if (userDetails != null) {
                val (actualUsername, actualEmail) = userDetails // Destructure the pair

                // Store user data in SharedPreferences
                val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString("username", actualUsername)
                    putString("email", actualEmail)
                    apply()
                }

                Toast.makeText(this, "Login Successful! Loading...", Toast.LENGTH_SHORT).show()

                val loadingIntent = Intent(this, LoadingScreen::class.java)
                loadingIntent.putExtra("TARGET_ACTIVITY", HomePage::class.java.name)
                startActivity(loadingIntent)
                finish()

            } else {
                Toast.makeText(this, "Invalid Username/Email or Password.", Toast.LENGTH_LONG).show()
            }
        }

        binding.facebookButton.setOnClickListener {
            Toast.makeText(this, "Facebook login will be implemented here.", Toast.LENGTH_SHORT).show()
        }

        binding.googleButton.setOnClickListener {
            Toast.makeText(this, "Google login will be implemented here.", Toast.LENGTH_SHORT).show()
        }
    }
}