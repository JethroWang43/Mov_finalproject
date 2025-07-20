package com.example.myapplication

import android.content.Intent // Import Intent for navigation
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: DatabaseHandler // Declare DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHandler(this) // Initialize DatabaseHandler

        // --- Handle Sign Up Button Click ---
        binding.signUpButton.setOnClickListener {
            val username = binding.usernameSignupInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordSignupInput.text.toString().trim()
            val confirmPassword = binding.confirmPasswordInput.text.toString().trim()

            // 1. Input Validation
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Exit the click listener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Basic email format validation (can be more robust with regex)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Optional: Password strength validation (e.g., minimum length)
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // 2. Check for Existing User (Username or Email)
            if (db.checkUserExists(username, email)) {
                Toast.makeText(this, "Username or Email already exists. Please Sign In or use another.", Toast.LENGTH_LONG).show()
            } else {
                // 3. Insert User
                // FIX: Explicitly name parameters for the User data class constructor
                val newUser = User(username = username, email = email, password = password)
                val result = db.insertData(newUser) // This already handles success/failure toasts

                if (result != -1.toLong()) { // If insertion was successful (DatabaseHandler returns -1 on failure)
                    // 4. Navigate to Sign In after successful registration
                    val intent = Intent(this, SignIn::class.java)
                    // Clear the back stack so user can't go back to SignUp with back button
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish() // Finish SignUp activity
                }
                // No need for an else here, as DatabaseHandler already shows a toast for failure
            }
        }

        // --- Handle "Sign In Now!" Text Click (Navigate to SignIn Activity) ---
        binding.signInNowText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish() // Optional: Finish SignUp activity so user goes directly to SignIn
        }

        // --- Handle "SIGN IN" link at the top right ---
        binding.signInLinkText.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            startActivity(intent)
            finish() // Optional: Finish SignUp activity
        }
    }
}