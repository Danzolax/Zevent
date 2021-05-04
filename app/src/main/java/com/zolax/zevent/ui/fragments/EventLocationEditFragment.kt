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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.ui.viewmodels.EventLocationEditViewModel
import com.zolax.zevent.util.Constants
import com.zolax.zevent.util.DialogUtil
import com.zolax.zevent.util.LocationRecommend
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_event_location_edit.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.mapView


@AndroidEntryPoint
class EventLocationEditFragment : Fragment(R.layout.fragment_event_location_edit) {
    private lateinit var event: Event
    private val gson = Gson()
    private var map: GoogleMap? = null
    private var isEditLocation = false
    private var newMarker: Marker? = null
    private val eventLocationEditViewModel: EventLocationEditViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        launchMap(savedInstanceState)
        event = gson.fromJson(requireArguments().getString("event"), Event::class.java)
        subscribeObservers()
        initButtons()

    }

    private fun initButtons() {
        auto_map_button.setOnClickListener {
            if (event.players!!.size > 2){
                setRecMarkerOnMap(LocationRecommend.getRecommendLocation(event.players as ArrayList<Player>),"Рекомендуемое место проведения")

            } else{
                Snackbar.make(requireView(),"Слишком мало людей",Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
            }
        }
    }

    private fun subscribeObservers() {
        eventLocationEditViewModel.isSuccessUpdateLocation.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success -> {
                    findNavController().popBackStack()
                    Snackbar.make(requireView(),"Место проведения изменено",Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Ошибка изменения места проведения",Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show()
                }
            }
        })
    }

    private fun launchMap(savedInstanceState: Bundle?) {
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(event.latitude!!, event.longitude!!), Constants.MAP_CAMERA_ZOOM
                )
            )
            googleMap.addMarker(
                MarkerOptions().position(
                    LatLng(
                        event.latitude!!,
                        event.longitude!!
                    )
                ).title("Место проведения мероприятия")
            )
            googleMap.setOnMapClickListener { location ->
                if (newMarker == null) {
                    isEditLocation = true
                    (requireActivity() as AppCompatActivity).invalidateOptionsMenu()
                    newMarker = setMarkerOnMap(location,"Новое место проведения мероприятия")
                } else {
                    newMarker!!.remove()
                    newMarker = setMarkerOnMap(location,"Новое место проведения мероприятия")
                }
            }
        }
    }

    private fun setMarkerOnMap(location: LatLng,title: String): Marker? {
        return map?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title(title)
        )
    }
    private fun setRecMarkerOnMap(location: LatLng,title: String): Marker? {
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                location, Constants.MAP_CAMERA_ZOOM
            )
        )
        return map?.addMarker(
            MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title(title)
        )
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Редактирование места"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        if (isEditLocation) {
            menu.findItem(R.id.action_confirm).isVisible = true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().popBackStack()
            }
            R.id.action_confirm ->{
                DialogUtil.buildConfirmDialog(
                    requireContext(),
                    "Вы точно хотите поменять место проведения мероприятия"
                ) { _, _ ->
                   eventLocationEditViewModel.updateEventLocationtById(event.id!!,(newMarker as Marker).position)
                }.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}