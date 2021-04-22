package com.zolax.zevent.ui.fragments



import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.zolax.zevent.R
import com.zolax.zevent.util.Constants.MAP_CAMERA_ZOOM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_map.*



@AndroidEntryPoint
class SimpleMapViewerFragment : Fragment(R.layout.fragment_simple_map_viewer) {
    private var map: GoogleMap? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchMap(savedInstanceState)
        setHasOptionsMenu(true)


    }


    private fun launchMap(savedInstanceState: Bundle?) {
        val latitude: Double = requireArguments().getDouble("latitude")
        val longitude: Double = requireArguments().getDouble("longitude")
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { googleMap ->
            map = googleMap
            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude), MAP_CAMERA_ZOOM
                )
            )
            googleMap.addMarker(MarkerOptions().position(LatLng(latitude,longitude)).title("Мероприятие"))
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Локация мероприятия"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().popBackStack()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
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