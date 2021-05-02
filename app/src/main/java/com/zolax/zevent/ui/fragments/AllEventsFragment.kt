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
import android.widget.ArrayAdapter
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
import com.zolax.zevent.util.Constants
import com.zolax.zevent.util.DialogUtil
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.filter_dialog.view.*
import kotlinx.android.synthetic.main.fragment_all_events.*
import timber.log.Timber
import java.lang.NumberFormatException
import javax.inject.Inject


@AndroidEntryPoint
class AllEventsFragment : Fragment(R.layout.fragment_all_events) {
    private val allEventsViewModel: AllEventsViewModel by viewModels()
    private lateinit var allEventsAdapter: AllEventsAdapter

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    lateinit var manager : LocationManager

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter(recycler_view)
        subscribeObservers()
        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
        initButtons()
    }

    private fun initButtons() {
        fab.setOnClickListener {
            initDialog()
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

    @SuppressLint("MissingPermission")
    private fun initDialog() {
        val view = layoutInflater.inflate(R.layout.filter_dialog,null)
        DialogUtil.buildFilterDialog(requireContext(),view){ _, _ ->
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    it?.let {
                        var currentPlayersCount: Int? = null
                        var allPlayersCount: Int? = null
                        if (view.current_players_count_filter.text.toString() != ""){
                            try {
                                currentPlayersCount =  Integer.parseInt(view.current_players_count_filter.text.toString())
                            } catch (e: NumberFormatException){
                                Snackbar.make(requireView(),"Не правильно введено количество игроков", Snackbar.LENGTH_SHORT)
                            }
                        }
                        if (view.all_players_count_filter.text.toString() != ""){
                            try {
                                allPlayersCount =  Integer.parseInt(view.all_players_count_filter.text.toString())
                            } catch (e: NumberFormatException){
                                Snackbar.make(requireView(),"Не правильно введено количество игроков", Snackbar.LENGTH_SHORT)
                            }
                        }
                        allEventsViewModel.getFilteredList(
                            FirebaseAuth.getInstance().uid!!,
                            LatLng(it.latitude, it.longitude),
                            view.category_filter.selectedItem as String,
                            view.date_filter.selectedItem as String,
                            view.is_need_equip_filter.isChecked,
                            currentPlayersCount,
                            allPlayersCount
                        )
                    }
                }
            } else {
                buildAlertMessageNoLocationService()
            }
        }.show()
        val categoryAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,
            Constants.CATEGORY_FILTER_LIST
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.category_filter.adapter = categoryAdapter
        val dateAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,
            Constants.DATE_FILTER_LIST
        )
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.date_filter.adapter = dateAdapter
    }


}