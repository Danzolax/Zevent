package com.zolax.zevent.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import kotlinx.android.synthetic.main.events_my_item.view.*
import java.util.*

class MyEventsAdapter : RecyclerView.Adapter<MyEventsAdapter.AllEventsViewHolder>() {

    inner class AllEventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var events: List<Event>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllEventsViewHolder {
        return AllEventsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.events_my_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllEventsViewHolder, position: Int) {
        val event = events[position]
        holder.itemView.apply {
            eventTitle.text = event.title
            eventType.text = event.category
            eventPlayersCount.text =
                "Количество игроков - ${event.players?.size}/${event.playersCount}"
            val calendar = Calendar.getInstance()
            calendar.time = event.eventDateTime
            setDateTimeInTextView(calendar, event_date, this.context)
            mapButton.setOnClickListener {
                val bundle = Bundle()
                bundle.putDouble("latitude", event.latitude!!)
                bundle.putDouble("longitude", event.longitude!!)
                findNavController().navigate(R.id.action_eventsFragment_to_simpleMapViewerFragment, bundle)
            }
            moreButton.setOnClickListener {
                val bundle = Bundle()
                val gson = Gson()
                bundle.putString("event", gson.toJson(event))
                findNavController().navigate(R.id.action_eventsFragment_to_myEventMoreFragment, bundle)
            }

        }
    }



    @SuppressLint("SetTextI18n")
    private fun setDateTimeInTextView(pickedDateTime: Calendar, view: TextView, context: Context) {
        view.text = DateUtils.formatDateTime(
            context,
            pickedDateTime.timeInMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_TIME
        )

    }


    override fun getItemCount() = events.size
}