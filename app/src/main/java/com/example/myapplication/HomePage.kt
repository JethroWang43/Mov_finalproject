package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.myapplication.databinding.ActivityHomePageBinding
import com.google.android.material.navigation.NavigationView

class HomePage : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityHomePageBinding
    private var selectedEntry: TextView? = null
    private val EDIT_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- DrawerLayout Setup START ---
        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        binding.ivProfile.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // --- IMPORTANT CHANGE: Retrieve and display user data ---
        val headerView = binding.navView.getHeaderView(0)
        val navUsername = headerView.findViewById<TextView>(R.id.nav_header_username)
        val navEmail = headerView.findViewById<TextView>(R.id.nav_header_email)

        // Retrieve stored username and email from SharedPreferences
        val sharedPref = getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Guest User") // Default "Guest User"
        val email = sharedPref.getString("email", "guest@example.com") // Default email

        navUsername.text = username
        navEmail.text = email
        // --- DrawerLayout Setup END ---

        // Set username on the home screen TextView (tvUsername)
        val homeUsernameTextView = findViewById<TextView>(R.id.tvUsername)
        homeUsernameTextView.text = "Welcome, $username!"



        // ➕ Add entry
        binding.floatingActionButton2.setOnClickListener {
            addDiaryEntry("Untitled", "New diary content...")
        }

        // 🔍 Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                for (i in 0 until binding.llDiaryContainer.childCount) {
                    val view = binding.llDiaryContainer.getChildAt(i)
                    if (view is TextView) {
                        val title = view.text.toString().lowercase()
                        view.visibility = if (title.contains(query)) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun addDiaryEntry(title: String, content: String) {
        val titleView = TextView(this).apply {
            text = title
            textSize = 18f
            setPadding(16, 16, 16, 16)
            setTextColor(0xFF000000.toInt()) // Black color
            setBackgroundColor(0xFFE0E0E0.toInt()) // Light grey background
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }

            tag = content

            setOnClickListener {
                selectedEntry = this
                val intent = Intent(this@HomePage, EditPage::class.java)
                intent.putExtra("entry_title", text.toString())
                intent.putExtra("entry_content", tag.toString())
                startActivityForResult(intent, EDIT_REQUEST_CODE)
            }
        }

        binding.llDiaryContainer.addView(titleView, 0)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                Toast.makeText(this, "You are on Home", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about_us -> {
                val intent = Intent(this, AboutTeam::class.java)
                startActivity(intent)
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
        with (sharedPref.edit()) {
            clear() // Clear all stored login data
            apply()
        }

        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, SignIn::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val updatedTitle = data?.getStringExtra("updated_title") ?: ""
                    val updatedContent = data?.getStringExtra("updated_content") ?: ""
                    selectedEntry?.text = updatedTitle
                    selectedEntry?.tag = updatedContent
                }
                Activity.RESULT_CANCELED -> {
                    selectedEntry?.let { binding.llDiaryContainer.removeView(it) }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}