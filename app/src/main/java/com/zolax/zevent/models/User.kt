package com.zolax.zevent.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class User(
    @DocumentId
    var id: String? = null ,
    var name: String? = null,
    var telephoneNumber: String? = null,
    var email:String? = null,
    var age: String? = null,
    var prefers: String? = null,
    var aboutMe: String? = null,
    var imageURI: String? = null,


) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel?, flags: Int) {
        parcel?.let {
            it.writeString(id)
            it.writeString(name)
            it.writeString(email)
            it.writeString(telephoneNumber)
            it.writeString(email)
            it.writeString(age)
            it.writeString(prefers)
            it.writeString(aboutMe)
            it.writeString(imageURI)
        }
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}
