package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoadingScreen : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the class name of the target activity from the Intent
        val targetActivityClassName = intent.getStringExtra("TARGET_ACTIVITY")

        // Use a Handler to delay the transition to the next activity
        Handler(Looper.getMainLooper()).postDelayed({
            if (targetActivityClassName != null) {
                try {
                    // Dynamically get the Class object from the string name
                    val targetActivityClass = Class.forName(targetActivityClassName) as Class<out AppCompatActivity>
                    val intent = Intent(this, targetActivityClass)

                    // If you passed any extra data from the previous activity to LoadingScreen,
                    // you can forward it to the target activity here:
                    // intent.putExtras(intent.extras ?: Bundle())

                    startActivity(intent)
                    finish() // Close LoadingScreen so it's not on the back stack
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    // Handle error if target activity class is not found
                    // Maybe redirect to a fallback like SignIn or show an error toast
                    Toast.makeText(this, "Error: Target activity not found.", Toast.LENGTH_LONG).show()
                }
            } else {
                // If no target activity is specified, redirect to a default screen (e.g., SignIn)
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
                finish()
            }
        }, SPLASH_TIME_OUT)
    }
}