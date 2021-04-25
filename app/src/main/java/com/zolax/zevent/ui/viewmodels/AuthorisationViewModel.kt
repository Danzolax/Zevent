package com.zolax.zevent.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*

import com.zolax.zevent.BaseApplication
import com.zolax.zevent.models.User
import com.zolax.zevent.repositories.FirebaseRepository
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class AuthorisationViewModel @ViewModelInject constructor(
    val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val signInData = MutableLiveData<Resource<Boolean>>()
    private val signUpData = MutableLiveData<Resource<Unit>>()

    val noMutableSignInData : LiveData<Resource<Boolean>> = signInData
    val noMutableSignUpData : LiveData<Resource<Unit>> = signUpData

    fun isAuthenticated() = firebaseRepository.isAuthenticated()

    fun signIn(email: String, password:String) = viewModelScope.launch {
        signInData.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        signInData.postValue(Resource.Loading())
        val response = firebaseRepository.signInWithEmail(email,password)
        signInData.postValue(response)
    }

    fun signUp(
        name: String,
        password: String ,
        telephoneNumber: String,
        email:String,
        age: String,
        prefers: String,
        aboutMe: String,
    ) = viewModelScope.launch {
        signUpData.value?.let {
            if (it is Resource.Loading){
                return@launch
            }
        }
        signUpData.postValue(Resource.Loading())
        for (field in listOf(name,telephoneNumber,email,age,prefers,aboutMe)){
            if (field.isEmpty()){
                signUpData.postValue(Resource.Error(msg = "Заполните все поля!!!"))
                return@launch
            }
        }
        if(name.length > 15){
            signUpData.postValue(Resource.Error(msg = "Слишком длинное имя!"))
            return@launch
        }

        val user = User().apply {
            this.email = email
            this.aboutMe = aboutMe
            try {
                age.toInt()
            } catch (error: NumberFormatException){
                signUpData.postValue(Resource.Error(msg = "Некорректный возраст"))
                return@launch
            }
            this.age = age
            this.name = name
            this.prefers = prefers
            try {
                telephoneNumber.toInt()
            }catch (error: NumberFormatException){
                signUpData.postValue(Resource.Error(msg = "Некорректный номер телефона"))
                return@launch
            }
            this.telephoneNumber = telephoneNumber
        }
        val response = firebaseRepository.signUpWithEmail(user, password)
        signUpData.postValue(response)
    }

    fun signOut() = firebaseRepository.signOut()
}