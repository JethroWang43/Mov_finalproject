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
    private lateinit var db: DatabaseHandler
    private var moodId: Int = -1
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHandler(this)

        // Get data from intent
        moodId = intent.getIntExtra("mood_id", -1)
        userId = intent.getIntExtra("user_id", -1)
        val title = intent.getStringExtra("entry_title") ?: ""
        val content = intent.getStringExtra("entry_content") ?: ""

        binding.etTitle.setText(title)
        binding.etContent.setText(content)

        // Save Button: Update or Insert mood
        binding.btnSave.setOnClickListener {
            val updatedTitle = binding.etTitle.text.toString()
            val updatedContent = binding.etContent.text.toString()

            if (moodId != -1) {
                // Update mood entry manually (delete + reinsert)
                db.deleteMoodById(moodId)
            }

            val success = db.insertMood(MoodEntry(0, updatedTitle, updatedContent, userId))

            if (success) {
                val resultIntent = Intent().apply {
                    putExtra("updated_title", updatedTitle)
                    putExtra("updated_content", updatedContent)
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }

        // Delete Button: Show confirmation dialog
        binding.btnDelete.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes") { _, _ ->
                    if (moodId != -1) {
                        db.deleteMoodById(moodId)
                    }
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Read-only switch
        binding.stRead.setOnCheckedChangeListener { _, isChecked ->
            binding.etTitle.isEnabled = !isChecked
            binding.etContent.isEnabled = !isChecked
            binding.btnSave.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE
            binding.btnDelete.visibility = if (isChecked) View.INVISIBLE else View.VISIBLE

            // Optional: force text color black
            binding.etTitle.setTextColor(0xFF000000.toInt())
            binding.etContent.setTextColor(0xFF000000.toInt())
        }
    }
}
