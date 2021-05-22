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

class ProfileViewModel @ViewModelInject constructor(private val firebaseRepository: FirebaseRepository) :
    ViewModel() {
    val profileData = MutableLiveData<Resource<User>>()
    val isEditProfile = MutableLiveData<Resource<Unit>>()
    val isSuccessUploadImage = MutableLiveData<Resource<Unit>>()
    val profileImage = MutableLiveData<Resource<Uri>>()

    fun getCurrentUser() = viewModelScope.launch {
        profileData.postValue(firebaseRepository.getCurrentUser())
    }

    fun updateUser(
        name: String,
        telephoneNumber: String,
        age: String,
        prefers: String,
        aboutMe: String,
    ) = viewModelScope.launch {
        if (isEditProfile.value is Resource.Loading){
            return@launch
        }
        isEditProfile.postValue(Resource.Loading())
        val response = firebaseRepository.updateUser(name,telephoneNumber,age,prefers,aboutMe)
        isEditProfile.postValue(response)
    }
    fun resetFlags(){
        isEditProfile.postValue(null)
        isSuccessUploadImage.postValue(null)
    }

    fun updateImageOfCurrentUser(uri: Uri) = viewModelScope.launch {
        if (isSuccessUploadImage.value is Resource.Loading){
            return@launch
        }
        isSuccessUploadImage.postValue(Resource.Loading())
        val response = firebaseRepository.updateImageOfCurrentUser(uri)
        isSuccessUploadImage.postValue(response)

    }
    fun downloadCurrentUserImage() = viewModelScope.launch {
        val response = firebaseRepository.downloadCurrentUserImage()
        profileImage.postValue(response)
    }

}