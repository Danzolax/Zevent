package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class EventLocationEditViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val isSuccessUpdateLocation = MutableLiveData<Resource<Unit>>()

    fun updateEventLocationtById(id: String,location: LatLng) = viewModelScope.launch {
        val response = firebaseRepository.updateEventLocationtById(id, location)
        isSuccessUpdateLocation.postValue(response)
    }
}