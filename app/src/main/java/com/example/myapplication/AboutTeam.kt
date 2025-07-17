package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityAboutTeamBinding

class AboutTeam : AppCompatActivity() {

    private lateinit var binding: ActivityAboutTeamBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply insets to the actual container (about_container in XML)
        ViewCompat.setOnApplyWindowInsetsListener(binding.aboutContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button listener
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
}
