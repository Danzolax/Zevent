package com.zolax.zevent.ui.viewmodels

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zolax.zevent.models.Player
import com.zolax.zevent.models.User
import com.zolax.zevent.models.Voting
import com.zolax.zevent.models.Votings
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch

class VotingsViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository,
) : ViewModel() {
    val votings = MutableLiveData<Resource<Votings>>()
    val isSuccessVoting = MutableLiveData<Resource<Unit>>()

    fun getVotings(userId: String) = viewModelScope.launch {
        votings.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        votings.postValue(Resource.Loading())
        val response = firebaseRepository.getVotings(userId)
        votings.postValue(response)
    }
    fun addScore(
        anUserId: String,
        category: String,
        role: String,
        votingPosition: Int,
        votingId: String
    ) = viewModelScope.launch {
        isSuccessVoting.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        isSuccessVoting.postValue(Resource.Loading())
        val response = firebaseRepository.addScore(anUserId, category, role, votingPosition, votingId)
        isSuccessVoting.postValue(response)
    }
}