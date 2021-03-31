package com.zolax.zevent.ui.fragments

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.material.snackbar.Snackbar
import com.zolax.zevent.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_map.*
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map), EasyPermissions.PermissionCallbacks {

    private var map: GoogleMap? = null

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            map?.isMyLocationEnabled = true
        }
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
            "Дай согласие мудила",
            0,
            ACCESS_COARSE_LOCATION,
            ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Snackbar.make(requireView(), "Помянем дурочка", Snackbar.LENGTH_LONG).show()
        } else {
            requestPermission()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        /* лежать + сосать */
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