package com.zolax.zevent.ui.viewmodels

import android.graphics.Bitmap
import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.User
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class AnotherUserProfileViewModel @ViewModelInject constructor(private val firebaseRepository: FirebaseRepository) :
    ViewModel() {
    val profileData = MutableLiveData<Resource<User>>()
    val profileImage = MutableLiveData<Resource<Uri>>()

    fun getUserById(id: String) = viewModelScope.launch {
        profileData.postValue(firebaseRepository.getUser(id))
    }

    fun downloadUserImageById(id:String) = viewModelScope.launch {
        val response = firebaseRepository.downloadUserImageById(id)
        profileImage.postValue(response)
    }

}