package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.ProfileViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*


@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val profileViewModel: ProfileViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
        subscribeObservers()
        profileViewModel.getCurrentUser()

    }

    private fun subscribeObservers() {
        profileViewModel.profileData.observe(viewLifecycleOwner, { result ->
            when(result){
                is Resource.Success ->{
                    telephoneNumber.text = result.data?.telephoneNumber ?: "Пусто"
                    name.text = result.data?.name ?: "Пусто"
                    age.text = result.data?.age ?: "Пусто"
                    prefers.text = result.data?.prefers ?: "Пусто"
                    aboutYourSelf.text = result.data?.aboutMe ?: "Пусто"
                }
                is Resource.Error ->{
                    Toast.makeText(requireContext(),"Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initButtons() {
        settings_btn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }
        edit_profile_btn.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }
}