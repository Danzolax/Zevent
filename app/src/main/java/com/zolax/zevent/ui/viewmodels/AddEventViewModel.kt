package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.auth.data.model.Resource
import com.zolax.zevent.models.Event
import com.zolax.zevent.repositories.FirebaseRepository

class AddEventViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel(){
    private val newEventData = MutableLiveData<Resource<Event>>()
}