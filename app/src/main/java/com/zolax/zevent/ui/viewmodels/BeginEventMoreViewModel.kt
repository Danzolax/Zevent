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

class BeginEventMoreViewModel @ViewModelInject constructor(
    val repository: FirebaseRepository
) : ViewModel() {
    val isSuccessUnsubscribe = MutableLiveData<Resource<Unit>>()
    val isSuccessDelete = MutableLiveData<Resource<Unit>>()
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
    fun addPlayersInVotingsAndDeleteBeginEvent(beginEventId: String, userId: String) = viewModelScope.launch {
        isSuccessDelete.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        isSuccessDelete.postValue(Resource.Loading())
        val response =  repository.addPlayersInVotingsAndDeleteBeginEvent(beginEventId,userId)
        isSuccessDelete.postValue(response)
    }

    fun deleteBeginEvent(beginEventId: String, userId: String) = viewModelScope.launch {
        isSuccessDelete.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        isSuccessDelete.postValue(Resource.Loading())
        val response =  repository.deleteBeginEvent(beginEventId,userId)
        isSuccessDelete.postValue(response)
    }
}