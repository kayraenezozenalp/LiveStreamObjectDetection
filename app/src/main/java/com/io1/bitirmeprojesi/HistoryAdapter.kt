package com.io1.bitirmeprojesi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.io1.bitirmeprojesi.data.model.HistoryModelItem
import com.squareup.picasso.Picasso

class HistoryAdapter(var historyList : ArrayList<HistoryModelItem>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(var view : View) : RecyclerView.ViewHolder(view) {
        val historyImage : ImageView
        val createdDate : TextView
        val historyID : TextView

        init {
            historyImage = view.findViewById(R.id.historyImage)
            createdDate = view.findViewById(R.id.createdTime)
            historyID = view.findViewById(R.id.historyID)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.history_item,parent,false)
        return HistoryViewHolder(view)
    }

    override fun getItemCount(): Int {

        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.createdDate.text = historyList[position].create_date
        holder.historyID.text = historyList[position].id.toString()

        Picasso.with(holder.view.context)
            .load(historyList[position].image_path)
            .into(holder.historyImage)
    }
}