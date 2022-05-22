package com.example.cryptobiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class TableAdapter (private val inflater: LayoutInflater)
    : ListAdapter<Int, TableViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : TableViewHolder {
        val row: View = inflater.inflate(R.layout.row, parent, false)
        return TableViewHolder(row)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        // TODO: get a record from the table
        //  format: String, String, Double->String with format rules and (+/-) signs

        holder.bindTo(arrayOf("", "", ""))
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<Int> = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldRow: Int, newRow: Int): Boolean {
                return oldRow == newRow
            }
            override fun areContentsTheSame(oldRow: Int, newRow: Int): Boolean {
                return areItemsTheSame(oldRow, newRow)
            }
        }
    }
}