package com.iamnippon.bmiandhealth.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iamnippon.bmiandhealth.R
import com.iamnippon.bmiandhealth.domain.model.BmiResult
import java.text.SimpleDateFormat
import java.util.*
class HistoryAdapter(
    private val onItemClick: (BmiResult) -> Unit,
    private val onDeleteClick: (BmiResult) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<BmiResult>()

    fun submitList(newList: List<BmiResult>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvTime)
        val tvBmi: TextView = view.findViewById(R.id.tvBmi)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val btnDelete: View = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bmi_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        holder.tvBmi.text = String.format("%.1f", item.bmi)
        holder.tvCategory.text = item.category
        holder.tvDate.text =
            SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                .format(Date(item.timestamp))

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount(): Int = items.size
}

