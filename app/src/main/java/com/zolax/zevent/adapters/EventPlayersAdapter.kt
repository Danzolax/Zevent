package com.zolax.zevent.adapters

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.zolax.zevent.R
import com.zolax.zevent.models.Player
import com.zolax.zevent.models.User
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.event_players_item.view.*
import javax.inject.Inject


class EventPlayersAdapter(
    private val isOtherType: Boolean,
    private val isShowTelephone: Boolean,
    private val navigateResId: Int
) : RecyclerView.Adapter<EventPlayersAdapter.EventPlayersViewHolder>() {
    @Inject
    lateinit var glide: RequestManager

    inner class EventPlayersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Triple<User,Player,Uri>>() {
        override fun areItemsTheSame(oldItem: Triple<User,Player,Uri>, newItem: Triple<User,Player,Uri>): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(oldItem: Triple<User,Player,Uri>, newItem: Triple<User,Player,Uri>): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var players: List<Triple<User,Player,Uri>>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventPlayersViewHolder {
        return EventPlayersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.event_players_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EventPlayersViewHolder, position: Int) {
        val player = players[position]
        holder.itemView.apply {
            //TODO Добавить инъекцию
            Glide.with(this).load(player.third).into(profileAvatar)
            name.text = if (position == 0) "Администратор: ${player.first.name}" else player.first.name
            if(!isShowTelephone){
                telephone_number.visibility = View.GONE
            } else{
                telephone_number.text = player.first.telephoneNumber
            }
            if(isOtherType){
                role.visibility = View.GONE
            } else{
                role.text = player.second.role
            }
            profileAvatar.setOnClickListener {
                val bundle =  Bundle()
                bundle.putString("userId", player.first.id)
                findNavController().navigate(navigateResId,bundle)
            }
        }
    }




    override fun getItemCount() = players.size
}