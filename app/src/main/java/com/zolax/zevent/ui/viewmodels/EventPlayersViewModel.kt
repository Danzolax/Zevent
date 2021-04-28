package com.zolax.zevent.ui.viewmodels

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Player
import com.zolax.zevent.models.User
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class EventPlayersViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val players = MutableLiveData<Resource<List<Triple<User, Player, Uri>>>>()

    fun getPlayersByEventId(id: String) = viewModelScope.launch {
        val response = firebaseRepository.getPlayersByEventId(id)
        players.postValue(response)
    }
}