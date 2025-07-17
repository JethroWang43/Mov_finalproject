package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityEditPageBinding

class EditPage : AppCompatActivity() {

    private lateinit var binding: ActivityEditPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPageBinding.inflate(layoutInflater)
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
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
