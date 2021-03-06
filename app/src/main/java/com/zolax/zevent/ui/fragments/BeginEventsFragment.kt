package com.zolax.zevent.ui.fragments


import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.zolax.zevent.R
import com.zolax.zevent.adapters.BeginEventsAdapter
import com.zolax.zevent.ui.viewmodels.BeginEventsViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_begin_events.*
import timber.log.Timber


@AndroidEntryPoint
class BeginEventsFragment : Fragment(R.layout.fragment_begin_events) {
    private val beginEventsViewModel: BeginEventsViewModel by viewModels()
    private lateinit var beginEventsAdapter: BeginEventsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        beginEventsViewModel.getAllBeginEventsByUserId(FirebaseAuth.getInstance().uid!!)
    }

    private fun subscribeObservers() {
        beginEventsViewModel.eventsData.observe(viewLifecycleOwner, { events ->
            when (events) {
                is Resource.Success -> {
                    beginEventsAdapter.events = events.data!!
                }
                is Resource.Error -> {
                    Snackbar.make(
                        requireView(),
                        "Ошибка загрузки мероприятий",
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
        })
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        beginEventsAdapter = BeginEventsAdapter()
        beginEventsAdapter.setOnMapButtonClickListener { event ->
            Timber.d("карта кнопка")
            val bundle = Bundle()
            bundle.putDouble("latitude", event.latitude!!)
            bundle.putDouble("longitude", event.longitude!!)
            findNavController().navigate(R.id.action_eventsFragment_to_simpleMapViewerFragment, bundle)
        }
        beginEventsAdapter.setOnMoreButtonClickListener { event ->
            Timber.d("больше кнопка")
            val bundle = Bundle()
            bundle.putString("eventId", event.id)
            findNavController().navigate(R.id.action_eventsFragment_to_beginEventMoreFragment, bundle)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = beginEventsAdapter
        }
    }

}