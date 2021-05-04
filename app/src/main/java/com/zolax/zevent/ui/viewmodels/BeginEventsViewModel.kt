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

class BeginEventsViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val eventsData = MutableLiveData<Resource<List<Event>>>()


    fun getAllBeginEventsByUserId(id: String) = viewModelScope.launch {
        val response = firebaseRepository.getAllBeginEventsByUserId(id)
        eventsData.postValue(response)
    }
}

