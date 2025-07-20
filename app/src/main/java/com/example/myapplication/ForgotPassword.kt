package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityForgotPasswordBinding // Import generated binding class

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var db: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = DatabaseHandler(this)

        // Set OnClickListener for the new "SIGN IN" text button
        binding.textButtonSignIn.setOnClickListener {
            val intent = Intent(this, SignIn::class.java)
            // Using CLEAR_TOP and NEW_TASK flags is a good practice when navigating back to a main entry point like SignIn
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Finish ForgotPassword activity so user can't come back to it with back button
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.emailInputForgot.text.toString().trim()
            val newPassword = binding.newPasswordInputForgot.text.toString().trim()
            val confirmPassword = binding.confirmNewPasswordInputForgot.text.toString().trim()

            // Input Validation
            if (email.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to update the password in the database
            val success = db.updateUserPassword(email, newPassword)

            if (success) {
                Toast.makeText(this, "Password successfully reset! Please sign in.", Toast.LENGTH_LONG).show()
                // Navigate back to the SignIn screen
                val intent = Intent(this, SignIn::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to reset password. Email not found.", Toast.LENGTH_LONG).show()
            }
        }
    }
}