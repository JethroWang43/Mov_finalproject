package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMoodPageBinding

class MoodPage : AppCompatActivity() {

    private lateinit var binding: ActivityMoodPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("entry_title") ?: ""
        val content = intent.getStringExtra("entry_content") ?: ""

        binding.etTitle.setText(title)
        binding.etContent.setText(content)

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("updated_title", binding.etTitle.text.toString())
                putExtra("updated_content", binding.etContent.text.toString())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.btnDelete.setOnClickListener {
            // Show confirmation dialog
            AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes") { _, _ ->
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
