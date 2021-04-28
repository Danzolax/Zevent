package com.zolax.zevent.ui.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.zolax.zevent.models.Event
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class AllEventsViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val eventsData = MutableLiveData<Resource<List<Event>>>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAllEventsReverseByUserId(id: String) = viewModelScope.launch {
        val response = firebaseRepository.getAllEventsReverseByUserId(id)
        eventsData.postValue(response)
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun getAllEventsReverseByUserIdWithRadius(id: String,userLocation: LatLng) = viewModelScope.launch {
        val response = firebaseRepository.getAllEventsReverseByUserIdWithRadius(id,userLocation)
        eventsData.postValue(response)
    }
}