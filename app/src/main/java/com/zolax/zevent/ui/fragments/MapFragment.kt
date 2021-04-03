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
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.zolax.zevent.R
import com.zolax.zevent.util.Constants.MAP_CAMERA_ZOOM
import com.zolax.zevent.util.Constants.REQUEST_LOCATION_AGAIN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.*
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), EasyPermissions.PermissionCallbacks{

    private var map: GoogleMap? = null

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        launchMap(savedInstanceState)


    }



    private fun launchMap(savedInstanceState: Bundle?){
        mapView?.onCreate(savedInstanceState)
        moveCameraToUserLocation()
        mapUISettings()
        initButtons()
        mapView?.getMapAsync { googleMap ->
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
            } else{
                map?.isMyLocationEnabled = true
            }
        }
    }
    private fun initButtons() {
        fab_location.setOnClickListener {
            moveCameraToUserLocation()
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun moveCameraToUserLocation() {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (locationManager.isLocationEnabled){
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude, it.longitude), MAP_CAMERA_ZOOM
                    )
                )
            }
        }
        else{
            buildAlertMessageNoLocationService()
        }

    }

    private fun buildAlertMessageNoLocationService() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
            .setMessage("Включите GPS для отслеживания вашего местоположения")
            .setPositiveButton("Включить"
            ) { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
        val alert: AlertDialog = builder.create()
        alert.show()
    }


    private fun mapUISettings() {
        map?.uiSettings?.isCompassEnabled = false
        map?.uiSettings?.isMyLocationButtonEnabled = false
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