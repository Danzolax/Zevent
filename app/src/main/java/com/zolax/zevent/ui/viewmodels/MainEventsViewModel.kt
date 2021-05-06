package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class MainEventsViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    val isVotingListNoEmpty = MutableLiveData<Resource<Boolean>>()

    fun checkVotingCount(userId: String) = viewModelScope.launch {
        val response = firebaseRepository.checkVotingCount(userId)
        isVotingListNoEmpty.postValue(response)
    }
}