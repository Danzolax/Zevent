package com.zolax.zevent.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.zolax.zevent.models.User
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import java.util.*

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val users = Firebase.firestore.collection("users")
    private val events = Firebase.firestore.collection("events")

    private inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> { //TODO как работает
        return try {
            action()
        } catch (e: Exception) {
            Timber.d(e)
            Resource.Error(e)
        }
    }

    suspend fun updateUserLocation(location: Location): Unit = withContext(Dispatchers.IO) {
        val geoPoint = GeoPoint(
            location.latitude, location.longitude
        )
        users.document(
            auth.uid!!
        ).update(
            hashMapOf<String, Any>("lastLocation" to geoPoint)
        ).await()
    }

    suspend fun signUpWithEmail(user: User, password: String) = withContext(Dispatchers.IO) {
        safeCall {
            val firebaseUser: FirebaseUser?
            val result = FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(user.email!!, password)
                .await()
            firebaseUser = result.user
            users.document(firebaseUser!!.uid).set(user).await()
            Resource.Success<Unit>()
        }

    }

    suspend fun signInWithEmail(email: String, password: String) = withContext(Dispatchers.IO) {
        safeCall {
            val firebaseUser: FirebaseUser?
            val result = auth
                .signInWithEmailAndPassword(email, password)
                .await()
            firebaseUser = result.user
            Resource.Success(firebaseUser != null)
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    fun isAuthenticated() = auth.currentUser != null

    fun getCurrentUserId(): String = auth.uid!!

    suspend fun getCurrentUser(): Resource<User> {
        return getUser(auth.uid!!)
    }

    suspend fun getUser(uid: String): Resource<User> = withContext(Dispatchers.IO) {
        safeCall {
            Resource.Success(
                users
                    .document(uid)
                    .get()
                    .await()
                    .toObject(User::class.java)
            )
        }
    }

    suspend fun updateUser(
        name: String,
        telephoneNumber: String,
        age: String,
        prefers: String,
        aboutMe: String,
    ) =
        safeCall {
            val firebaseUser = getUser(auth.uid!!)
            firebaseUser.data?.aboutMe = aboutMe
            firebaseUser.data?.age = age
            firebaseUser.data?.name = name
            firebaseUser.data?.telephoneNumber = telephoneNumber
            firebaseUser.data?.prefers = prefers
            users.document(auth.uid!!).set(firebaseUser.data!!).await()
            Resource.Success<Unit>()
        }

//    suspend fun uploadCurrentUserProfileImage() = safeCall {
//
//    }

    suspend fun updateImageOfCurrentUser(uri: Uri) = safeCall {
        Timber.d("start fun ")
        Timber.d("start update user image uri:$uri")
        storageRef.child(getCurrentUserId()).putFile(uri).await()
        Resource.Success<Unit>()
    }

//        Timber.d("end update user image")
//        val imageUri = res.metadata?.reference?.downloadUrl?.await()
//        Timber.d("URI: $imageUri")
//        users.document(firebaseUser.uid).update("imageURI",imageUri).await()
//        Timber.d("End fun")



    suspend fun downloadCurrentUserImage() =
        safeCall {
            val maxDownloadSize = 5L * 1024 * 1024
            val url = storageRef.child(getCurrentUserId()).downloadUrl.await()
            Resource.Success(url)
        }


}