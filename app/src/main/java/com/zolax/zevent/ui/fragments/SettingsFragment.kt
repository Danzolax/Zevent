package com.zolax.zevent.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.zolax.zevent.R
import com.zolax.zevent.ui.activities.LoginActivity
import com.zolax.zevent.ui.viewmodels.AuthorisationViewModel
import com.zolax.zevent.util.Constants
import com.zolax.zevent.util.DialogUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.event_search_radius_dialog.*
import kotlinx.android.synthetic.main.event_search_radius_dialog.view.*
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import java.util.zip.Inflater

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val authorisationViewModel: AuthorisationViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
        setHasOptionsMenu(true)
    }

    private fun initButtons() {
        signOut.setOnClickListener {
            authorisationViewModel.signOut()
            startActivity(Intent(requireContext(),LoginActivity::class.java))
            requireActivity().finish()

        }
        changeEventsSearchRadius.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.event_search_radius_dialog,null)
            DialogUtil.buildDialogWithView(requireContext(),"Выбор расстояния",view){_,_ ->
                val mySharedPreferences = requireActivity().getSharedPreferences(Constants.APP_PREFERENCES,
                    Context.MODE_PRIVATE)
                when(view.radioGroup.checkedRadioButtonId){
                    R.id.radio_min ->{
                        mySharedPreferences.edit().putInt(Constants.APP_PREFERENCES_EVENT_SEARCH_RADIUS, 5000).apply()

                    }
                    R.id.radio_mid ->{
                        mySharedPreferences.edit().putInt(Constants.APP_PREFERENCES_EVENT_SEARCH_RADIUS, 15000).apply()
                    }
                    R.id.radio_max ->{
                        mySharedPreferences.edit().putInt(Constants.APP_PREFERENCES_EVENT_SEARCH_RADIUS, 30000).apply()
                    }
                }
                Snackbar.make(requireView(),"Радиус поиска изменен", Snackbar.LENGTH_SHORT).show()
            }.show()
        }

    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Настройки"
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
}