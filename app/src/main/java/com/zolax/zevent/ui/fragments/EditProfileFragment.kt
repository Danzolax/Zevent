package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.ProfileViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.aboutYourSelf
import kotlinx.android.synthetic.main.fragment_edit_profile.age
import kotlinx.android.synthetic.main.fragment_edit_profile.name
import kotlinx.android.synthetic.main.fragment_edit_profile.prefers
import kotlinx.android.synthetic.main.fragment_edit_profile.profileAvatar
import kotlinx.android.synthetic.main.fragment_edit_profile.telephoneNumber
import kotlinx.android.synthetic.main.fragment_profile.*

@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    val profileViewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initButtons()
        subscribeObservers()
        initViews();
    }

    private fun initViews() {
        profileViewModel.getCurrentUser()
    }

    private fun subscribeObservers() {
        profileViewModel.isEditProfile.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is Resource.Error -> {
                    progressBar.isVisible = false
                    Toast
                        .makeText(requireContext(), "Ошибка сохранения данных", Toast.LENGTH_SHORT)
                        .show()
                }


                is Resource.Success -> {
                    progressBar.isVisible = false
                    findNavController().popBackStack()

                }
                is Resource.Loading ->
                    progressBar.isVisible = true
            }
        })
        profileViewModel.profileData.observe(viewLifecycleOwner, { result ->
            when(result){
                is Resource.Success ->{
                    telephoneNumber.setText(result.data?.telephoneNumber ?: "Пусто")
                    name.setText(result.data?.name ?: "Пусто")
                    age.setText(result.data?.age ?: "Пусто")
                    prefers.setText(result.data?.prefers ?: "Пусто")
                    aboutYourSelf.setText(result.data?.aboutMe ?: "Пусто")
                }
                is Resource.Error ->{
                    Toast.makeText(requireContext(),"Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun initButtons() {
        back_btn.setOnClickListener {
            findNavController().popBackStack()
        }
        accept_btn.setOnClickListener {
            profileViewModel.updateUser(
                name.text.toString(),
                telephoneNumber.text.toString(),
                age.text.toString(),
                prefers.text.toString(),
                aboutYourSelf.text.toString()
            )
            profileViewModel.getCurrentUser()
        }
        profileAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "Аватар изменен", Toast.LENGTH_SHORT).show()
        }
    }
}