package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

import com.zolax.zevent.R
import com.zolax.zevent.adapters.AllEventsAdapter
import com.zolax.zevent.ui.viewmodels.AllEventsViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_all_events.*
import timber.log.Timber


@AndroidEntryPoint
class AllEventsFragment : Fragment(R.layout.fragment_all_events) {
    private val allEventsViewModel: AllEventsViewModel by viewModels()
    private lateinit var allEventsAdapter: AllEventsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        allEventsViewModel.getAllEvents()
    }

    private fun subscribeObservers() {
        allEventsViewModel.eventsData.observe(viewLifecycleOwner, {events ->
            when(events){
                is Resource.Success ->{
                    Timber.d("Load: ${events.data}")
                    allEventsAdapter.events = events.data!!
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Ошибка загрузки мероприятий",Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    private fun initAdapter(rv: RecyclerView) {
        allEventsAdapter = AllEventsAdapter()
        rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allEventsAdapter
        }
    }




}