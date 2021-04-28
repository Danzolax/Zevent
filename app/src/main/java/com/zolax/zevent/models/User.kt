package com.zolax.zevent.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class User(
    @DocumentId
    var id: String? = null,
    var name: String? = null,
    var telephoneNumber: String? = null,
    var email: String? = null,
    var age: String? = null,
    var prefers: String? = null,
    var aboutMe: String? = null,
    var imageURI: String? = null,
    var distanceToSearchEvents: Int? = null,
)

