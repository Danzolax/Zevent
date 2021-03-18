package com.zolax.zevent.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.zolax.zevent.R
import com.zolax.zevent.ui.activities.LoginActivity
import com.zolax.zevent.ui.viewmodels.AuthorisationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val authorisationViewModel: AuthorisationViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
    }

    private fun initButtons() {
        back_btn.setOnClickListener {
            findNavController().popBackStack()
        }
        signOut.setOnClickListener {
            authorisationViewModel.signOut()
            startActivity(Intent(requireContext(),LoginActivity::class.java))
            requireActivity().finish()

        }

    }
}