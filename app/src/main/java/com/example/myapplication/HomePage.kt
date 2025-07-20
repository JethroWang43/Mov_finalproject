package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityHomePageBinding
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomePage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomePageBinding
    private lateinit var db: DatabaseHandler
    private lateinit var moodEntryAdapter: MoodEntryAdapter
    private var currentUserId: Int = -1

    // Store all entries fetched from the DB, to be filtered by search
    private var allMoodEntries: List<MoodEntry> = emptyList()

    private var currentSelectedDate: String = "" // This is primarily for adding new entries to a specific date
    private val ADD_ENTRY_REQUEST_CODE = 100
    private val EDIT_ENTRY_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHandler(this)

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        currentSelectedDate = dateFormat.format(calendar.time)

        // --- User ID and Header Setup ---
        val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Guest User")
        val email = sharedPref.getString("email", "guest@example.com")

        currentUserId = db.getUserId(username ?: "")

        // Set Username/Email in Drawer Header
        val headerView = binding.navView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.nav_header_username)
        val navEmail = headerView.findViewById<TextView>(R.id.nav_header_email)

        navUsername.text = username
        navEmail.text = email

        // Show welcome on home page
        binding.tvUsername.text = "Welcome, $username!"

        // --- Calendar View Setup ---
        binding.cvCalendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            currentSelectedDate = dateFormat.format(selectedCalendar.time)
            Toast.makeText(this, "Selected Date: $currentSelectedDate", Toast.LENGTH_SHORT).show()
            // When a date is selected, we want to show entries ONLY for that date.
            // Clear search bar and then load entries for the selected date.
            binding.etSearch.setText("") // Clear search when date is selected
            loadMoodEntriesForSelectedDate()
        }

        // --- Drawer setup ---
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // The ImageView for opening the drawer
        binding.ivProfile.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // --- RecyclerView Setup ---
        binding.rvMoodEntries.layoutManager = LinearLayoutManager(this)
        // Pass a lambda to MoodEntryAdapter for handling edit clicks
        moodEntryAdapter = MoodEntryAdapter(emptyList()) { entryToEdit ->
            val intent = Intent(this, MoodPage::class.java).apply {
                putExtra("is_edit_mode", true)
                putExtra("entry_id", entryToEdit.id)
                putExtra("entry_date", entryToEdit.date)
                putExtra("entry_title", entryToEdit.title)
                putExtra("entry_content", entryToEdit.content)
            }
            startActivityForResult(intent, EDIT_ENTRY_REQUEST_CODE)
        }
        binding.rvMoodEntries.adapter = moodEntryAdapter

        // --- Load initial mood entries (all entries) ---
        loadAllMoodEntries() // Initially load all entries

        // ➕ Add entry FAB click listener
        binding.floatingActionButton2.setOnClickListener {
            val intent = Intent(this, MoodPage::class.java).apply {
                // When adding a new entry, use the date currently selected in the calendar
                putExtra("selected_date", currentSelectedDate)
            }
            startActivityForResult(intent, ADD_ENTRY_REQUEST_CODE)
        }

        // 🔍 Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase(Locale.getDefault()).trim() // Trim query
                if (query.isEmpty()) {
                    // If search query is empty, show all entries
                    loadAllMoodEntries()
                } else {
                    // If there's a query, filter the 'allMoodEntries' list
                    val filteredList = allMoodEntries.filter {
                        it.title.lowercase(Locale.getDefault()).contains(query) ||
                                it.content.lowercase(Locale.getDefault()).contains(query)
                    }
                    moodEntryAdapter.updateData(filteredList)
                    updateEmptyMessage(filteredList.isEmpty() && query.isNotBlank(), "No entries found matching your search.")
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Function to load all mood entries for the current user
    private fun loadAllMoodEntries() {
        if (currentUserId != -1) {
            allMoodEntries = db.getMoodEntriesForUser(currentUserId) // Fetch all entries
            moodEntryAdapter.updateData(allMoodEntries)
            updateEmptyMessage(allMoodEntries.isEmpty(), "No diary entries yet. Start writing!")
        } else {
            Toast.makeText(this, "Error: User not logged in to load entries.", Toast.LENGTH_SHORT).show()
            updateEmptyMessage(true, "Please log in to see your diary entries.")
        }
    }

    // Function to load mood entries ONLY for the currently selected date
    private fun loadMoodEntriesForSelectedDate() {
        if (currentUserId != -1) {
            val entriesForDate = db.getMoodEntriesForUserAndDate(currentUserId, currentSelectedDate)
            moodEntryAdapter.updateData(entriesForDate)
            updateEmptyMessage(entriesForDate.isEmpty(), "No diary entries for $currentSelectedDate. Tap '+' to add one!")
        } else {
            Toast.makeText(this, "Error: User not logged in to load entries.", Toast.LENGTH_SHORT).show()
            updateEmptyMessage(true, "Please log in to see your diary entries.")
        }
    }

    // Helper function to manage the empty message visibility and text
    private fun updateEmptyMessage(isEmpty: Boolean, message: String) {
        if (isEmpty) {
            binding.rvMoodEntries.visibility = View.GONE
            binding.tvNoEntriesMessage.visibility = View.VISIBLE
            binding.tvNoEntriesMessage.text = message
        } else {
            binding.rvMoodEntries.visibility = View.VISIBLE
            binding.tvNoEntriesMessage.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ADD_ENTRY_REQUEST_CODE || requestCode == EDIT_ENTRY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Entry saved/updated!", Toast.LENGTH_SHORT).show()
                // After saving/updating, always reload all entries and apply current search filter
                val currentQuery = binding.etSearch.text.toString().trim()
                if (currentQuery.isEmpty()) {
                    loadAllMoodEntries() // Reload all if search is empty
                } else {
                    // Re-run the filter with the existing query
                    val filteredList = db.getMoodEntriesForUser(currentUserId).filter {
                        it.title.lowercase(Locale.getDefault()).contains(currentQuery) ||
                                it.content.lowercase(Locale.getDefault()).contains(currentQuery)
                    }
                    allMoodEntries = db.getMoodEntriesForUser(currentUserId) // Also refresh the master list
                    moodEntryAdapter.updateData(filteredList)
                    updateEmptyMessage(filteredList.isEmpty() && currentQuery.isNotBlank(), "No entries found matching your search.")
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Operation cancelled or entry deleted.", Toast.LENGTH_SHORT).show()
                // If cancelled or deleted, refresh all entries (and re-apply search if any)
                val currentQuery = binding.etSearch.text.toString().trim()
                if (currentQuery.isEmpty()) {
                    loadAllMoodEntries()
                } else {
                    val filteredList = db.getMoodEntriesForUser(currentUserId).filter {
                        it.title.lowercase(Locale.getDefault()).contains(currentQuery) ||
                                it.content.lowercase(Locale.getDefault()).contains(currentQuery)
                    }
                    allMoodEntries = db.getMoodEntriesForUser(currentUserId)
                    moodEntryAdapter.updateData(filteredList)
                    updateEmptyMessage(filteredList.isEmpty() && currentQuery.isNotBlank(), "No entries found matching your search.")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // When coming back to HomePage, ensure all entries are loaded by default
        // and current search filter is applied if any.
        val currentQuery = binding.etSearch.text.toString().trim()
        if (currentQuery.isEmpty()) {
            loadAllMoodEntries()
        } else {
            // If there's a query, first load all entries, then apply the filter
            allMoodEntries = db.getMoodEntriesForUser(currentUserId)
            val filteredList = allMoodEntries.filter {
                it.title.lowercase(Locale.getDefault()).contains(currentQuery) ||
                        it.content.lowercase(Locale.getDefault()).contains(currentQuery)
            }
            moodEntryAdapter.updateData(filteredList)
            updateEmptyMessage(filteredList.isEmpty() && currentQuery.isNotBlank(), "No entries found matching your search.")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                Toast.makeText(this, "You are already on Home", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about_us -> {
                startActivity(Intent(this, AboutTeam::class.java))
            }
            R.id.nav_logout -> {
                performLogout()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun performLogout() {
        val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()

        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, SignIn::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}