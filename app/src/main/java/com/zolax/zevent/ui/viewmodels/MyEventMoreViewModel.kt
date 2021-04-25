package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Player
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class MyEventMoreViewModel @ViewModelInject constructor(
    val repository: FirebaseRepository
) : ViewModel() {
    val isSuccessUnsubscribe = MutableLiveData<Resource<Unit>>()

    fun unsubscribeEventById(id: String, player: Player) = viewModelScope.launch {
        val response = repository.unsubscribeEventById(id,player)
        isSuccessUnsubscribe.postValue(response)
    }

    fun deleteEventById(id: String) = viewModelScope.launch {
        val response = repository.deleteEventById(id)
        isSuccessUnsubscribe.postValue(response)
    }
}