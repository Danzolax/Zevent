package com.zolax.zevent.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.adapters.MyEventsAdapter
import com.zolax.zevent.ui.viewmodels.MyEventsViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_my_events.*
import timber.log.Timber

@AndroidEntryPoint
class MyEventsFragment : Fragment(R.layout.fragment_my_events) {
    private val myEventsViewModel: MyEventsViewModel by viewModels()
    private lateinit var myEventsAdapter: MyEventsAdapter
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        myEventsViewModel.getAllEventsByUserId(FirebaseAuth.getInstance().uid!!)
    }

    private fun subscribeObservers() {
        myEventsViewModel.eventsData.observe(viewLifecycleOwner, {events ->
            when(events){
                is Resource.Success ->{
                    Timber.d("Load: ${events.data}")
                    myEventsAdapter.events = events.data!!
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Ошибка загрузки мероприятий", Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        myEventsAdapter = MyEventsAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myEventsAdapter
        }

    }
}