package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val etSearch = findViewById<EditText>(R.id.etSearch)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        val diaryContainer = findViewById<LinearLayout>(R.id.llDiaryContainer)

        tvUsername.text = "Jethro"

        ivProfile.setOnClickListener {
            // TODO: Profile logic
        }

        // 🔍 Search logic
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                for (i in 0 until diaryContainer.childCount) {
                    val entry = diaryContainer.getChildAt(i)
                    if (entry is TextView) {
                        val content = entry.text.toString().lowercase()
                        entry.visibility = if (content.contains(query)) TextView.VISIBLE else TextView.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // ➕ Add new entry
        fab.setOnClickListener {
            val newEntry = TextView(this).apply {
                text = "📝 New diary entry added!"
                setBackgroundColor(0xFFE0E0E0.toInt())
                setTextColor(0xFF000000.toInt())
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 8)
                }
            }
            diaryContainer.addView(newEntry, 0)
        }
    }
}
