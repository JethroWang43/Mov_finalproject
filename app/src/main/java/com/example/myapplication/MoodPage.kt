package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent // Import Intent for navigation
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityMoodPageBinding

class MoodPage : AppCompatActivity() {

    private lateinit var binding: ActivityMoodPageBinding
    private lateinit var db: DatabaseHandler // Declare DatabaseHandler
    private var entryId: Long = -1L // -1L indicates a new entry, otherwise it's an existing entry's ID from DB
    private var userId: Int = -1 // Store the ID of the current user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHandler(this) // Initialize DatabaseHandler

        // Retrieve user ID from SharedPreferences
        val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val loggedInUsername = sharedPref.getString("username", null) // Get logged-in username
        if (loggedInUsername != null) {
            // Get actual user ID from the database using the username
            userId = db.getUserId(loggedInUsername)
            if (userId == -1) {
                // Handle error: User not found in DB, this should ideally not happen if login worked
                Toast.makeText(this, "Error: User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
                finish() // Close MoodPage if user ID is critical and missing
                return
            }
        } else {
            Toast.makeText(this, "You must be logged in to create or edit diary entries.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Retrieve data passed from HomePage
        entryId = intent.getLongExtra("entry_id", -1L)
        val receivedDate = intent.getStringExtra("selected_date") ?: "No Date Selected"
        val title = intent.getStringExtra("entry_title") ?: ""
        val content = intent.getStringExtra("entry_content") ?: ""

        // Display the date in the TextView
        binding.tvEntryDate.text = receivedDate

        // Populate title and content fields
        binding.etTitle.setText(title)
        binding.etContent.setText(content)

        // If it's an existing entry, and the content/title are pre-filled,
        binding.stRead.isChecked = false // Ensure it starts in editable mode by default


        // Save button action
        binding.btnSave.setOnClickListener {
            val updatedTitle = binding.etTitle.text.toString().trim()
            val updatedContent = binding.etContent.text.toString()

            // Input validation: Title cannot be empty
            if (updatedTitle.isEmpty()) {
                Toast.makeText(this, "Diary title cannot be empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val resultIntent = Intent()
            var saveSuccess = false

            if (entryId == -1L) {
                // This is a new entry: Insert it into the database
                val moodEntry = MoodEntry(
                    id = 0, // Explicitly pass 0 for new entries; DB will auto-increment
                    userId = userId, // Link entry to the current user
                    date = receivedDate, // Use the date passed from HomePage
                    title = updatedTitle,
                    content = updatedContent
                )
                val newRowId = db.insertMoodEntry(moodEntry)
                if (newRowId != -1L) {
                    Toast.makeText(this, "Entry saved successfully!", Toast.LENGTH_SHORT).show()
                    resultIntent.putExtra("updated_title", updatedTitle)
                    resultIntent.putExtra("updated_content", updatedContent)
                    resultIntent.putExtra("entry_id", newRowId) // Return the new ID
                    setResult(Activity.RESULT_OK, resultIntent)
                    saveSuccess = true
                } else {
                    Toast.makeText(this, "Failed to save entry.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // This is an existing entry: Update it in the database
                val moodEntry = MoodEntry(
                    id = entryId, // Use the existing entryId
                    userId = userId, // Link entry to the current user
                    date = receivedDate, // Use the date passed from HomePage
                    title = updatedTitle,
                    content = updatedContent
                )
                val updated = db.updateMoodEntry(moodEntry)
                if (updated) {
                    Toast.makeText(this, "Entry updated successfully!", Toast.LENGTH_SHORT).show()
                    // Pass existing ID and updated data back to HomePage
                    resultIntent.putExtra("updated_title", updatedTitle)
                    resultIntent.putExtra("updated_content", updatedContent)
                    resultIntent.putExtra("entry_id", entryId) // Return the existing ID
                    setResult(Activity.RESULT_OK, resultIntent)
                    saveSuccess = true
                } else {
                    Toast.makeText(this, "Failed to update entry.", Toast.LENGTH_SHORT).show()
                }
            }

            if (saveSuccess) {
                finish() // Close MoodPage after successful save/update
            }
        }

        // Delete button with confirmation dialog
        binding.btnDelete.setOnClickListener {
            // Only show dialog and attempt delete if it's an existing entry
            if (entryId != -1L) {
                AlertDialog.Builder(this)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton("Yes") { _, _ ->
                        val deleted = db.deleteMoodEntry(entryId)
                        if (deleted) {
                            Toast.makeText(this, "Entry deleted.", Toast.LENGTH_SHORT).show()
                            val resultIntent = Intent().apply {
                                putExtra("entry_id", entryId) // Return ID to HomePage for removal
                            }
                            // Use CANCELED to indicate deletion, and also pass the ID for HomePage to remove it from its list
                            setResult(Activity.RESULT_CANCELED, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this, "Failed to delete entry.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                // If it's a new entry and delete is pressed, it means discard.
                Toast.makeText(this, "New entry discarded.", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED) // Indicate that the new entry was cancelled
                finish()
            }
        }

        // Switch logic to toggle read-only mode
        binding.stRead.setOnCheckedChangeListener { _, isChecked ->
            binding.etTitle.isEnabled = !isChecked
            binding.etContent.isEnabled = !isChecked

            if (isChecked) {
                // Read-only mode: hide buttons
                binding.btnSave.visibility = View.INVISIBLE
                binding.btnDelete.visibility = View.INVISIBLE
                // Force text color to black (assuming EditText's default text color might be different when disabled)
                binding.etTitle.setTextColor(0xFF000000.toInt())
                binding.etContent.setTextColor(0xFF000000.toInt())
            } else {
                // Editing mode: show buttons
                binding.btnSave.visibility = View.VISIBLE
                binding.btnDelete.visibility = View.VISIBLE
                // Restore text color
                binding.etTitle.setTextColor(0xFF000000.toInt()) // Assuming black for editable text
                binding.etContent.setTextColor(0xFF000000.toInt()) // Assuming black for editable text
            }
        }
    }
}