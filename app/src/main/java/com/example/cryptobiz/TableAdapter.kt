package com.example.cryptobiz

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.cryptobiz.Converters.currencyIntToString

class TableAdapter(private val inflater: LayoutInflater) :
    ListAdapter<QuotationEntity, TableViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val row: View = inflater.inflate(R.layout.row, parent, false)
        return TableViewHolder(row)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.bindTo(
            parseDateTime(getItem(position).datetime),
            currencyIntToString(getItem(position).quoteRUB),
            calculateDelta(position)
        )
    }

    companion object {
        private val DIFF_CALLBACK: DiffUtil.ItemCallback<QuotationEntity> =
            object : DiffUtil.ItemCallback<QuotationEntity>() {
                override fun areItemsTheSame(
                    oldRow: QuotationEntity,
                    newRow: QuotationEntity
                ): Boolean {
                    return oldRow == newRow
                }

                override fun areContentsTheSame(
                    oldRow: QuotationEntity,
                    newRow: QuotationEntity
                ): Boolean {
                    return areItemsTheSame(oldRow, newRow)
                }
            }
    }

    private fun parseDateTime(timestamp: String): String {
        val month = timestamp.subSequence(5, 7)
        val day = timestamp.subSequence(8, 10)
        val hour = timestamp.subSequence(11, 13)
        val minute = timestamp.subSequence(14, 16)

        return "$day.$month $hour:$minute"
    }

    // may need a rework in case of calculating delta for any currency
    private fun calculateDelta(position: Int): String {
        var deltaShow = ""
        if (position + 1 < itemCount && getItem(position + 1).quoteRUB > 0) {
            var delta =
                getItem(position).quoteRUB.toDouble() / getItem(position + 1).quoteRUB.toDouble() - 1
            var sign = "+"
            if (delta < 0) {
                delta = -delta
                sign = "-"
            }
            val deltaConverted = (delta * 1000000).toInt()
            if (deltaConverted > 0) {
                deltaShow = "$sign${currencyIntToString(deltaConverted, 4)}%"
            }
        }
        return deltaShow
    }
}