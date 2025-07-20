package com.example.myapplication

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodEntryAdapter(
    private var entries: List<MoodEntry>,
    private val onEditClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodEntryAdapter.MoodEntryViewHolder>() {

    class MoodEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.tvEntryItemDate)
        val titleTextView: TextView = itemView.findViewById(R.id.tvEntryItemTitle)
        val contentPreviewTextView: TextView = itemView.findViewById(R.id.tvEntryItemContentPreview)
        val editIcon: ImageView = itemView.findViewById(R.id.ivEditEntry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodEntryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood_entry, parent, false)
        return MoodEntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodEntryViewHolder, position: Int) {
        val entry = entries[position]
        holder.dateTextView.text = entry.date
        holder.titleTextView.text = entry.title
        holder.contentPreviewTextView.text = entry.content.take(100) + if (entry.content.length > 100) "..." else ""

        holder.editIcon.setOnClickListener {
            onEditClick(entry)
        }
    }

    override fun getItemCount(): Int = entries.size

    fun updateData(newEntries: List<MoodEntry>) {
        this.entries = newEntries
        notifyDataSetChanged()
    }
}