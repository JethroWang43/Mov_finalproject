package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding
    private var selectedEntry: TextView? = null
    private val EDIT_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnabout.setOnClickListener {
            val intent = Intent(this, AboutTeam::class.java)
            startActivity(intent)
        }

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
            setTextColor(0xFF000000.toInt())
            setBackgroundColor(0xFFE0E0E0.toInt())
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
}