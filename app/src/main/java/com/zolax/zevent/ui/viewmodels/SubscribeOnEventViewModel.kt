package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class SubscribeOnEventViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val event = MutableLiveData<Resource<Event>>()

    fun subscribeEventById(id: String, player: Player) = viewModelScope.launch {
        val response = firebaseRepository.subscribeEventById(id, player)
        event.postValue(response)
    }
}