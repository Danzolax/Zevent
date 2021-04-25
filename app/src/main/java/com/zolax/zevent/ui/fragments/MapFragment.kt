package com.zolax.zevent.ui.fragments


import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.zolax.zevent.R
import com.zolax.zevent.models.Event
import com.zolax.zevent.ui.viewmodels.MapViewModel
import com.zolax.zevent.util.Constants.MAP_CAMERA_ZOOM
import com.zolax.zevent.util.Constants.REQUEST_LOCATION_AGAIN
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.*
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), EasyPermissions.PermissionCallbacks,
    GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    private val mapViewModel: MapViewModel by viewModels()
    private var map: GoogleMap? = null

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        launchMap(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        mapViewModel.getAllEventsReverseByUserId(FirebaseAuth.getInstance().uid!!)
    }

    private fun subscribeObservers() {
        mapViewModel.eventsData.observe(viewLifecycleOwner, { result ->
            when (result) {
                is Resource.Success -> {
                    result.data?.forEach {elem ->
                        setMarkerOnMap(elem)
                    }
                }
                is Resource.Error ->{
                    Snackbar.make(requireView(),"Невозможно загрузить мероприятия",Snackbar.LENGTH_SHORT)
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

    private fun setMarkerOnMap(event: Event) {
        val marker: Marker? = map?.addMarker(MarkerOptions()
            .position(LatLng(event.latitude!!,event.longitude!!))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            .title(event.title))
        marker?.tag = event

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        Timber.d("marker clicked!")
        p0?.let {
            if (it.tag is Event){
                val bundle = Bundle()
                val gson = Gson()
                bundle.putString("event",gson.toJson(it.tag))
                findNavController().navigate(R.id.action_mapFragment_to_subscribeOnEventFragment, bundle)
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
            moveCameraToUserLocation()
        }
    }


    @SuppressLint("NewApi", "MissingPermission")
    private fun moveCameraToUserLocation() {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isLocationEnabled) {
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