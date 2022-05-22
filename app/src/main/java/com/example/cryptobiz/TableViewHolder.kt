package com.example.cryptobiz

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val textViewDatetime = itemView.findViewById<TextView>(R.id.datetime)
    private val textViewQuote = itemView.findViewById<TextView>(R.id.quote)
    private val textViewDelta = itemView.findViewById<TextView>(R.id.delta)

    fun bindTo(record: Array<String>) {
        textViewDatetime.text = record[0]
        textViewQuote.text = record[1]
        textViewDelta.text = record[2]
    }
}