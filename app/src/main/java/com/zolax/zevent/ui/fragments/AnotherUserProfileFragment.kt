package com.zolax.zevent.ui.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.AnotherUserProfileViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_another_user_profile.*
import javax.inject.Inject


@AndroidEntryPoint
class AnotherUserProfileFragment : Fragment(R.layout.fragment_another_user_profile) {
    private val profileViewModel: AnotherUserProfileViewModel by activityViewModels()
    @Inject
    lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
        profileViewModel.getUserById(requireArguments().getString("userId")!!)
        profileViewModel.downloadUserImageById(requireArguments().getString("userId")!!)

    }

    private fun subscribeObservers() {
        profileViewModel.profileData.observe(viewLifecycleOwner, { result ->
            when(result){
                is Resource.Success ->{
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
        profileViewModel.profileImage.observe(viewLifecycleOwner,{result ->
            when(result){
                is Resource.Success ->{
                    glide.load(result.data).into(profileAvatar)
                    profileAvatar.scaleType = ImageView.ScaleType.FIT_XY
                }
                is Resource.Error ->{
                    Toast.makeText(requireContext(),"Ошибка загрузки профиля", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Профиль игрока"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home ->{
                findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}