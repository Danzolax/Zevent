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
import timber.log.Timber

class BeginEventMoreViewModel @ViewModelInject constructor(
    val repository: FirebaseRepository
) : ViewModel() {
    val isSuccessUnsubscribe = MutableLiveData<Resource<Unit>>()
    val currentEvent = MutableLiveData<Resource<Event>>()

    fun unsubscribeBeginEventById(id: String, player: Player) = viewModelScope.launch {
        val response = repository.unsubscribeBeginEventById(id,player)
        isSuccessUnsubscribe.postValue(response)
    }



    fun getBeginEventById(id: String) = viewModelScope.launch {
        val response = repository.getBeginEventById(id)
        response.let {
            currentEvent.postValue(it)
        }
    }
}