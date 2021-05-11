package com.zolax.zevent.adapters


import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import kotlinx.android.synthetic.main.events_my_item.view.*


class BeginEventsAdapter : RecyclerView.Adapter<BeginEventsAdapter.AllEventsViewHolder>() {

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

    private var onMapButtonClickListener: ((Event) -> Unit)? = null
    private var onMoreButtonClickListener: ((Event) -> Unit)? = null

    fun setOnMapButtonClickListener(i:((Event) -> Unit)){
        onMapButtonClickListener = i
    }

    fun setOnMoreButtonClickListener(i:((Event) -> Unit)){
        onMoreButtonClickListener = i
    }

    override fun onBindViewHolder(holder: AllEventsViewHolder, position: Int) {
        val event = events[position]
        holder.itemView.apply {
            eventTitle.text = event.title
            eventType.text = event.category
            eventPlayersCount.text =
                "Количество игроков - ${event.players?.size}/${event.playersCount}"
            event_date.visibility = View.GONE
            mapButton.setOnClickListener {
                onMapButtonClickListener?.let { click ->
                    click(event)
                }
            }
            moreButton.setOnClickListener {
                onMoreButtonClickListener?.let { click ->
                    click(event)
                }
            }

        }
    }

    override fun getItemCount() = events.size
}