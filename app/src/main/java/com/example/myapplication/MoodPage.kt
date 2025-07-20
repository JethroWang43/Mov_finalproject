package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
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

        // Save button action
        binding.btnSave.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("updated_title", binding.etTitle.text.toString())
                putExtra("updated_content", binding.etContent.text.toString())
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        // Delete button with confirmation dialog
        binding.btnDelete.setOnClickListener {
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

        // Switch logic to toggle read-only mode
        binding.stRead.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Enable read-only mode
                binding.etTitle.isEnabled = false
                binding.etContent.isEnabled = false
                binding.btnSave.visibility = View.INVISIBLE
                binding.btnDelete.visibility = View.INVISIBLE

                // Force text color to 0xFF000000 (pure black with full opacity)
                binding.etTitle.setTextColor(0xFF000000.toInt())
                binding.etContent.setTextColor(0xFF000000.toInt())
            } else {
                // Enable editing mode
                binding.etTitle.isEnabled = true
                binding.etContent.isEnabled = true
                binding.btnSave.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE

                // Keep or restore text color to black
                binding.etTitle.setTextColor(0xFF000000.toInt())
                binding.etContent.setTextColor(0xFF000000.toInt())
            }
        }

    }
}
