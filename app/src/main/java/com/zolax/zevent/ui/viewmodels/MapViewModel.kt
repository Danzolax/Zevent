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
import timber.log.Timber

class MapViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository

) : ViewModel() {
    val eventsData = MutableLiveData<Resource<List<Event>>>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAllEventsReverseByUserId(id: String) = viewModelScope.launch {
        val response = firebaseRepository.getAllEventsReverseByUserId(id)
        eventsData.postValue(response)
    }

    fun getAllEventsReverseByUserIdWithRadius(id: String, userLocation: LatLng,radius: Int) =
        viewModelScope.launch {
            val response =
                firebaseRepository.getAllEventsReverseByUserIdWithRadius(id, userLocation,radius)
            eventsData.postValue(response)
        }

    fun getFilteredList(
        userId: String,
        location: LatLng,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?,
        radius: Int
    ) = viewModelScope.launch {
        val response = firebaseRepository.getFilteredList(
            userId,
            location,
            category,
            date,
            isNeedEquip,
            currentPlayersCount,
            allPlayersCount,
            radius
        )
        Timber.d("${response.data}")
        eventsData.postValue(response)
    }


    fun moveEventsToBeginByUserID(id: String) = viewModelScope.launch {
        firebaseRepository.moveEventsToBeginByUserID(id)
    }
}