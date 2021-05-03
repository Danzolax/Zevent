package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.adapters.EventPlayersAdapter
import com.zolax.zevent.models.Event
import com.zolax.zevent.ui.viewmodels.MyEventPlayersViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_event_players.*

@AndroidEntryPoint
class MyEventPlayersFragment : Fragment(R.layout.fragment_event_players) {
    private val myEventPlayersViewModel : MyEventPlayersViewModel by viewModels()
    private lateinit var eventPlayersAdapter:EventPlayersAdapter
    private lateinit var event: Event
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initAdapter(recycler_view)
        subscribeObservers()
        myEventPlayersViewModel.getPlayersByEventId(event.id!!)
    }

    private fun subscribeObservers() {
        myEventPlayersViewModel.players.observe(viewLifecycleOwner,{ result ->
            when(result){
                is Resource.Success ->{
                    eventPlayersAdapter.players = result.data!!
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Ошибка загрузки участников мероприятия", Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        val gson = Gson()
        event =  gson.fromJson(requireArguments().getString("event"), Event::class.java)
        eventPlayersAdapter = EventPlayersAdapter(event.category == "Другое",true,
            isMyEvent = true
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventPlayersAdapter
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Участники"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}