package com.zolax.zevent.ui.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.ProfileViewModel
import com.zolax.zevent.util.Constants.PICK_IMAGE_REQUEST
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.aboutYourSelf
import kotlinx.android.synthetic.main.fragment_edit_profile.age
import kotlinx.android.synthetic.main.fragment_edit_profile.name
import kotlinx.android.synthetic.main.fragment_edit_profile.prefers
import kotlinx.android.synthetic.main.fragment_edit_profile.profileAvatar
import kotlinx.android.synthetic.main.fragment_edit_profile.telephoneNumber
import javax.inject.Inject


@AndroidEntryPoint
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    val profileViewModel: ProfileViewModel by activityViewModels()
    var filePath: Uri? = null
    @Inject
    lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        initButtons()
        subscribeObservers()
        initViews();
    }



    private fun initViews() {
        profileViewModel.getCurrentUser()
    }

    private fun subscribeObservers() {
        profileViewModel.isEditProfile.observe(viewLifecycleOwner, { result ->
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
            when (result) {
                is Resource.Success -> {
                    telephoneNumber.setText(result.data?.telephoneNumber ?: "Пусто")
                    name.setText(result.data?.name ?: "Пусто")
                    age.setText(result.data?.age ?: "Пусто")
                    prefers.setText(result.data?.prefers ?: "Пусто")
                    aboutYourSelf.setText(result.data?.aboutMe ?: "Пусто")
                }
                is Resource.Error -> {
                    Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
        profileViewModel.isSuccessUploadImage.observe(viewLifecycleOwner,{result->
            when(result){
                is Resource.Success ->{

                }
                is Resource.Error ->{
                    Toast.makeText(context,"Фото не загружено", Toast.LENGTH_SHORT).show()

                }

            }
        })


    }

    private fun initButtons() {
        profileAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK , MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                intent,
                PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val imageUri = data?.data ?: return
            filePath = imageUri
            profileAvatar.setImageURI(imageUri)
            profileAvatar.scaleType = ImageView.ScaleType.FIT_XY
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (requireActivity() as AppCompatActivity).supportActionBar?.let {
            it.title = "Изменение профиля"
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
        menu.findItem(R.id.action_confirm).isVisible = true

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
            R.id.action_confirm ->{
                profileViewModel.updateUser(
                    name.text.toString(),
                    telephoneNumber.text.toString(),
                    age.text.toString(),
                    prefers.text.toString(),
                    aboutYourSelf.text.toString(),
                )
                if(filePath != null){
                    profileViewModel.updateImageOfCurrentUser(filePath!!)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
}