package com.zolax.zevent.util

import com.google.firebase.messaging.FirebaseMessagingService
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {



    override fun onNewToken(p0: String) {
        Timber.d("New token $p0")
    }



}