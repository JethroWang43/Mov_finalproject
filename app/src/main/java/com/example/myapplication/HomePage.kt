package com.example.myapplication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Backward-compatible system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set username
        binding.tvUsername.text = "Jethro"

        // Handle profile icon
        binding.ivProfile.setOnClickListener {
            // TODO: Open profile
        }

        // Search functionality
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                for (i in 0 until binding.llDiaryContainer.childCount) {
                    val entry = binding.llDiaryContainer.getChildAt(i)
                    if (entry is TextView) {
                        val content = entry.text.toString().lowercase()
                        entry.visibility = if (content.contains(query)) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Add diary entry
        binding.floatingActionButton2.setOnClickListener {
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
            binding.llDiaryContainer.addView(newEntry, 0)
        }
    }
}
