package com.zolax.zevent.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

import com.zolax.zevent.R
import com.zolax.zevent.adapters.AllEventsAdapter
import com.zolax.zevent.ui.viewmodels.AllEventsViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_all_events.*
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class AllEventsFragment : Fragment(R.layout.fragment_all_events) {
    private val allEventsViewModel: AllEventsViewModel by viewModels()
    private lateinit var allEventsAdapter: AllEventsAdapter

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        if ((requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager).isLocationEnabled) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                it?.let {
                    allEventsViewModel.getAllEventsReverseByUserIdWithRadius(FirebaseAuth.getInstance().uid!!,
                        LatLng(it.latitude,it.longitude)
                    )
                }
            }
        } else {
            buildAlertMessageNoLocationService()
        }
    }



    private fun subscribeObservers() {
        allEventsViewModel.eventsData.observe(viewLifecycleOwner, { events ->
            when (events) {
                is Resource.Success -> {
                    Timber.d("Load: ${events.data}")
                    allEventsAdapter.events = events.data!!
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

    private fun initAdapter(rv: RecyclerView) {
        allEventsAdapter = AllEventsAdapter()
        rv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allEventsAdapter
        }
    }

    private fun buildAlertMessageNoLocationService() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
            .setMessage("Включите GPS для отслеживания вашего местоположения")
            .setPositiveButton(
                "Включить"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        val alert: AlertDialog = builder.create()
        alert.show()
    }


}