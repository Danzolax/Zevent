package com.zolax.zevent.ui.activities


import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.zolax.zevent.R
import com.zolax.zevent.ui.viewmodels.AuthorisationViewModel
import com.zolax.zevent.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_registration.*


@AndroidEntryPoint
class RegistrationActivity : AppCompatActivity() {
    private val authorisationViewModel: AuthorisationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        progressBar.isVisible = false
        initButtons()
        subscribeToObservers()


    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun subscribeToObservers() {
        authorisationViewModel.noMutableSignUpData.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    progressBar.isVisible = false
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Resource.Error -> {
                    progressBar.isVisible = false
                    val msg = when (result.error) {
                        is FirebaseAuthWeakPasswordException ->
                            "Слабый пароль"
                        is FirebaseAuthInvalidCredentialsException ->
                            "Неправильный формат почты"
                        is FirebaseAuthUserCollisionException ->
                            "Такой пользователь уже зарегистрирован"
                        else -> null
                    } ?: (result.msg ?: "Неизвестная ощибка")
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                    progressBar.isVisible = false
                }
                is Resource.Loading -> {
                    progressBar.isVisible = true
                }
            }
        })
    }

    private fun initButtons() {
        if (loginPassword.text.toString() != regPasswordConfirm.text.toString()){
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }
        regButton.setOnClickListener {
            val imageUri: Uri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + resources.getResourcePackageName(R.drawable.empty_profile_photo)
                        + '/' + resources.getResourceTypeName(R.drawable.empty_profile_photo) + '/' + resources.getResourceEntryName(
                    R.drawable.empty_profile_photo
                )
            )
            authorisationViewModel.signUp(
                regName.text.toString(),
                regPasswordConfirm.text.toString(),
                regTelephone.text.toString(),
                regEmail.text.toString(),
                regAge.text.toString(),
                regPrefers.text.toString(),
                regAboutYourself.text.toString(),
                imageUri
            )
        }
        enterAccountButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


}

