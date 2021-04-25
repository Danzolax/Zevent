package com.zolax.zevent.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer

import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.AuthorisationViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.enterAccountButton
import kotlinx.android.synthetic.main.activity_login.regButton



@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val authorisationViewModel: AuthorisationViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (authorisationViewModel.isAuthenticated()){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }


        setContentView(R.layout.activity_login)
        initButtons()
        subscribeToObservers()

    }

    private fun subscribeToObservers() {
        authorisationViewModel.noMutableSignInData.observe(this, Observer { result ->
            when (result){
                is Resource.Success ->{
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Resource.Error ->{
                    Toast.makeText(this, "Ошибка входа", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading ->{

                }
            }
        })
    }

    private fun initButtons() {
        regButton.setOnClickListener {
            startActivity(Intent(this,RegistrationActivity::class.java))
            finish()
        }
        enterAccountButton.setOnClickListener {
            authorisationViewModel.signIn(
                loginEmail.text.toString(),
                loginPassword.text.toString()
            )
        }
    }


}