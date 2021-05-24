package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Event
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class AddEventViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val isSuccessCreateEvent = MutableLiveData<Resource<Event>>()

    fun addEvent(event: Event) = viewModelScope.launch {
        val response = firebaseRepository.addEvent(event)
        isSuccessCreateEvent.postValue(response)
    }


}