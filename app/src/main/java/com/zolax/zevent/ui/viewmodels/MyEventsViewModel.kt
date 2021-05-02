package com.zolax.zevent.ui.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Event
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class MyEventsViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val eventsData = MutableLiveData<Resource<List<Event>>>()


    @RequiresApi(Build.VERSION_CODES.N)
    fun getAllEventsByUserId(id: String) = viewModelScope.launch {
        val response = firebaseRepository.getAllEventsByUserId(id)
        eventsData.postValue(response)
    }

    fun getFilteredListByUserId(
        id: String,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?
    ) = viewModelScope.launch {
        val response = firebaseRepository.getFilteredListByUserId(id, category, date, isNeedEquip, currentPlayersCount, allPlayersCount)
        eventsData.postValue(response)
    }
}