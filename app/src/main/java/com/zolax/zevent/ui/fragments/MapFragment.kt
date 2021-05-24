package com.zolax.zevent.ui.fragments


import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
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
import com.zolax.zevent.ui.viewmodels.MapViewModel
import com.zolax.zevent.util.Constants
import com.zolax.zevent.util.Constants.MAP_CAMERA_ZOOM
import com.zolax.zevent.util.Constants.REQUEST_LOCATION_AGAIN
import com.zolax.zevent.util.DialogUtil
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.filter_dialog.view.*
import kotlinx.android.synthetic.main.fragment_map.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), EasyPermissions.PermissionCallbacks,
    GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private val mapViewModel: MapViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var manager: LocationManager
    private lateinit var mySharedPreferences: SharedPreferences

    private val markerList = mutableListOf<Marker>()


    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mySharedPreferences = requireActivity().getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE)
        requestPermission()
        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        launchMap(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                it?.let {
                    mapViewModel.getAllEventsWithRadius(
                        LatLng(it.latitude, it.longitude),
                        mySharedPreferences.getInt(
                            Constants.APP_PREFERENCES_EVENT_SEARCH_RADIUS,
                            Constants.DEFAULT_EVENT_SEARCH_RADIUS
                        )
                    )
                }
            }
        } else {
            buildAlertMessageNoLocationService()
        }

        mapViewModel.moveEventsToBeginByUserID(FirebaseAuth.getInstance().uid!!)
    }

    private fun subscribeObservers() {
        mapViewModel.eventsData.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Success -> {
                    clearMap()
                    result.data?.forEach { event ->
                        if (event.players!!.find { it.userId == FirebaseAuth.getInstance().uid!! } != null){
                            setMarkerOnMap(event,BitmapDescriptorFactory.HUE_RED)
                        } else{
                            setMarkerOnMap(event,BitmapDescriptorFactory.HUE_BLUE)

                        }
                    }
                }
                is Resource.Error -> {
                    Snackbar.make(
                        requireView(),
                        "Невозможно загрузить мероприятия",
                        Snackbar.LENGTH_SHORT
                    )
                }
            }
        })
    }

    override fun onMapClick(p0: LatLng?) {
        if (p0 != null) {
            val bundle = Bundle()
            bundle.putDouble("latitude", p0.latitude)
            bundle.putDouble("longitude", p0.longitude)
            findNavController().navigate(R.id.action_mapFragment_to_addEventFragment, bundle)
        } else {
            Snackbar.make(requireView(), "Выберите другую локацию!", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun clearMap() {
        if (markerList.isNotEmpty()) {
            markerList.forEach { it.remove() }
        }
    }

    private fun setMarkerOnMap(event: Event,color: Float) {

        val marker: Marker? = map?.addMarker(
            MarkerOptions()
                .position(LatLng(event.latitude!!, event.longitude!!))
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .title(event.title)
        )
        marker?.let {
            it.tag = event
            markerList.add(it)
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        Timber.d("marker clicked!")
        p0?.let { marker ->
            if (marker.tag is Event) {
                val buffEvent = marker.tag as Event
                val bundle = Bundle()
                val gson = Gson()
                if(buffEvent.players!!.find { it.userId == FirebaseAuth.getInstance().uid!! } != null){
                    bundle.putString("eventId", buffEvent.id)
                    findNavController().navigate(
                        R.id.action_mapFragment_to_myEventMoreFragment,
                        bundle
                    )
                } else{
                    bundle.putString("event", gson.toJson(marker.tag))
                    findNavController().navigate(
                        R.id.action_mapFragment_to_subscribeOnEventFragment,
                        bundle
                    )
                }
                return true
            }
        }
        return false
    }


    private fun launchMap(savedInstanceState: Bundle?) {
        mapView?.onCreate(savedInstanceState)
        moveCameraToUserLocation()
        mapUISettings()
        initButtons()
        mapView?.getMapAsync { googleMap ->
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            googleMap.setOnMapClickListener(this)
            googleMap.setOnMarkerClickListener(this)
            map = googleMap
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            } else {
                map?.isMyLocationEnabled = true
            }
        }
    }

    private fun initButtons() {
        fab_location.setOnClickListener {
            animateCameraToUserLocation()
        }
        fab_filter.setOnClickListener {
            initDialog()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initDialog() {
        val view = layoutInflater.inflate(R.layout.filter_dialog, null)
        DialogUtil.buildDialogWithView(requireContext(), "Фильтры", view) { _, _ ->
            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                    it?.let {
                        var currentPlayersCount: Int? = null
                        var allPlayersCount: Int? = null
                        if (view.current_players_count_filter.text.toString() != "") {
                            try {
                                currentPlayersCount =
                                    Integer.parseInt(view.current_players_count_filter.text.toString())
                            } catch (e: NumberFormatException) {
                                Snackbar.make(
                                    requireView(),
                                    "Не правильно введено количество игроков",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (view.all_players_count_filter.text.toString() != "") {
                            try {
                                allPlayersCount =
                                    Integer.parseInt(view.all_players_count_filter.text.toString())
                            } catch (e: NumberFormatException) {
                                Snackbar.make(
                                    requireView(),
                                    "Не правильно введено количество игроков",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                        mapViewModel.getFilteredEvents(
                            LatLng(it.latitude, it.longitude),
                            view.category_filter.selectedItem as String,
                            view.date_filter.selectedItem as String,
                            view.is_need_equip_filter.isChecked,
                            currentPlayersCount,
                            allPlayersCount,
                            mySharedPreferences.getInt(
                                Constants.APP_PREFERENCES_EVENT_SEARCH_RADIUS,
                                Constants.DEFAULT_EVENT_SEARCH_RADIUS
                            )
                        )
                    }
                }
            } else {
                buildAlertMessageNoLocationService()
            }
        }.show()
        val categoryAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            Constants.CATEGORY_FILTER_LIST
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.category_filter.adapter = categoryAdapter
        val dateAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item,
            Constants.DATE_FILTER_LIST
        )
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.date_filter.adapter = dateAdapter
    }


    @SuppressLint("MissingPermission")
    private fun animateCameraToUserLocation() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                it?.let {
                    map?.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), MAP_CAMERA_ZOOM
                        )
                    )
                }

            }
        } else {
            buildAlertMessageNoLocationService()
        }

    }

    @SuppressLint("MissingPermission")
    private fun moveCameraToUserLocation() {
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                it?.let {
                    map?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), MAP_CAMERA_ZOOM
                        )
                    )
                }

            }
        } else {
            buildAlertMessageNoLocationService()
        }

    }


    private fun mapUISettings() {
        map?.uiSettings?.isCompassEnabled = false
        map?.uiSettings?.isMyLocationButtonEnabled = false
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Карта"
            it.setHomeButtonEnabled(false)
            it.setDisplayHomeAsUpEnabled(false)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
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


    private fun requestPermission() {
        if (EasyPermissions.hasPermissions(
                requireContext(),
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION
            )
        ) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            REQUEST_LOCATION_AGAIN,
            0,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Snackbar.make(requireView(), "Отслеживание включено", Snackbar.LENGTH_LONG).show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        /*Empty*/
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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