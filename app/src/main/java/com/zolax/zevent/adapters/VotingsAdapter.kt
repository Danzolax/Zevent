package com.zolax.zevent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.models.Voting
import com.zolax.zevent.ui.viewmodels.VotingsViewModel
import kotlinx.android.synthetic.main.votings_item.view.*


class VotingsAdapter(
    val votingsViewModel: VotingsViewModel
) : RecyclerView.Adapter<VotingsAdapter.VotingsViewHolder>() {

    lateinit var votingsId: String



    inner class VotingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Voting>() {
        override fun areItemsTheSame(oldItem: Voting, newItem: Voting): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Voting, newItem: Voting): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    var votings: List<Voting>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VotingsViewHolder {
        return VotingsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.votings_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VotingsViewHolder, position: Int) {
        val voting = votings[position]
        holder.itemView.apply {
            event_title.text = voting.eventTitle
            event_category.text = voting.eventCategory
            player_name.text = voting.userName
            player_role.text = voting.player!!.role
            positive_button.setOnClickListener{
                votingsViewModel.addScore(
                    voting.player!!.userId!!,
                    voting.eventCategory!!,
                    voting.player!!.role!!,
                    position,
                    votingsId
                )
            }

        }
    }




    override fun getItemCount() = votings.size
}