package com.zolax.zevent.repositories

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.zolax.zevent.models.Event
import com.zolax.zevent.models.Player
import com.zolax.zevent.models.User
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FirebaseRepository {
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private val users = Firebase.firestore.collection("users")
    private val events = Firebase.firestore.collection("events")

    private inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
        return try {
            action()
        } catch (e: Exception) {
            Timber.d(e)
            Resource.Error(e)
        }
    }


    suspend fun signUpWithEmail(user: User, password: String, uri: Uri) =
        withContext(Dispatchers.IO) {
            safeCall {
                val firebaseUser: FirebaseUser?
                val result = FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(user.email!!, password)
                    .await()
                firebaseUser = result.user
                users.document(firebaseUser!!.uid).set(user).await()
                storageRef.child(firebaseUser.uid).putFile(uri).await()
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

    private fun getCurrentUserId(): String = auth.uid!!

    suspend fun getCurrentUser(): Resource<User> {
        return getUser(auth.uid!!)
    }

    private suspend fun getUser(uid: String): Resource<User> = withContext(Dispatchers.IO) {
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

    //TODO фото по умолчанию
    suspend fun updateImageOfCurrentUser(uri: Uri) = safeCall {
        Timber.d("start fun ")
        Timber.d("start update user image uri:$uri")
        storageRef.child(getCurrentUserId()).putFile(uri).await()
        Resource.Success<Unit>()
    }

    suspend fun downloadCurrentUserImage() =
        safeCall {
            val url = storageRef.child(getCurrentUserId()).downloadUrl.await()
            Resource.Success(url)
        }


    suspend fun addEvent(event: Event) =
        safeCall {
            events.add(event).await()
            Resource.Success<Unit>()
        }

    suspend fun getAllEvents() = safeCall {
        Timber.d("load all events...")
        val events = events.get().await().toObjects(Event::class.java)
        Timber.d("events: $events")
        Resource.Success(events)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getAllEventsByUserId(id: String) = safeCall {
        val events = events.get().await().toObjects(Event::class.java)
        Timber.d("load all events by $id")
        Timber.d("$events")
        events.removeIf {
            var flag = false
            it.players?.forEach { elem ->
                if (elem.userId.equals(id)) {
                    flag = true
                    return@forEach
                }
            }
            return@removeIf !flag
        }
        Timber.d("$events")
        Resource.Success(events)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getAllEventsReverseByUserId(id: String) = safeCall {
        val events = events.get().await().toObjects(Event::class.java)
        Timber.d("load all events by $id")
        Timber.d("$events")
        events.removeIf {
            var flag = false
            it.players?.forEach { elem ->
                if (elem.userId.equals(id)) {
                    flag = true
                    return@forEach
                }
            }
            return@removeIf flag
        }
        events.removeIf {
            it.players?.size == it.playersCount
        }
        Timber.d("$events")
        Resource.Success(events)
    }

    suspend fun getAllEventsReverseByUserIdWithRadius(id: String, userLocation: LatLng) = safeCall {
        val events = events.get().await().toObjects(Event::class.java)
        Timber.d("load all events by $id")
        Timber.d("$events")
        events.removeIf {
            var flag = false
            it.players?.forEach { elem ->
                if (elem.userId.equals(id)) {
                    flag = true
                    return@forEach
                }
            }
            return@removeIf flag
        }
        events.removeIf {
            it.players?.size == it.playersCount
        }
        events.removeIf {
            distance(
                userLocation.latitude,
                userLocation.longitude,
                it.latitude!!,
                it.longitude!!,
            ) > 5000
        }
        Timber.d("$events")
        Resource.Success(events)
    }

    private fun distance(lat_a: Double, lng_a: Double, lat_b: Double, lng_b: Double): Double {
        val earthRadius = 3958.75
        val latDiff = Math.toRadians((lat_b - lat_a))
        val lngDiff = Math.toRadians((lng_b - lng_a))
        val a = sin(latDiff / 2) * sin(latDiff / 2) +
                cos(Math.toRadians(lat_a)) * cos(Math.toRadians(lat_b)) *
                sin(lngDiff / 2) * sin(lngDiff / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadius * c
        val meterConversion = 1609
        return (distance * meterConversion.toDouble())
    }

    suspend fun subscribeEventById(id: String, player: Player) = safeCall {
        val event = events.document(id).get().await().toObject(Event::class.java)
        (event?.players as ArrayList).add(player)
        events.document(id).set(event).await()
        Resource.Success<Unit>()
    }

    suspend fun unsubscribeEventById(id: String, player: Player) = safeCall {
        val event = events.document(id).get().await().toObject(Event::class.java)
        (event?.players as ArrayList).remove(player)
        events.document(id).set(event).await()
        Resource.Success<Unit>()
    }

    suspend fun deleteEventById(id: String) = safeCall {
        events.document(id).delete().await()
        Resource.Success<Unit>()
    }

    suspend fun updateEventLocationtById(id: String, location: LatLng) = safeCall {
        val event = events.document(id).get().await().toObject(Event::class.java)
        event?.let {
            it.latitude = location.latitude
            it.longitude = location.longitude
            events.document(id).set(it).await()
        }
        Resource.Success<Unit>()
    }

    suspend fun getEventById(id: String) = safeCall {
        val event = events.document(id).get().await().toObject(Event::class.java)
        Resource.Success(event)
    }

    private suspend fun downloadUserImage(id: String) = storageRef.child(id).downloadUrl.await()


    suspend fun getPlayersByEventId(id: String) = safeCall {
        val players: ArrayList<Triple<User, Player, Uri>> = arrayListOf()
        val event = events.document(id).get().await().toObject(Event::class.java)
        event!!.players!!.forEach { player ->
            players.add(
                Triple(
                    getUser(player.userId!!).data!!,
                    player,
                    downloadUserImage(player.userId!!),
                )
            )
        }
        Resource.Success(players)
    }

    suspend fun getFilteredList(
        id: String,
        location: LatLng,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?
    ) = safeCall {
        val response = getAllEventsReverseByUserIdWithRadius(id, location)
        if (response is Resource.Error){
            return@safeCall response
        }
        var eventList = response.data!!
        if (category != "Не фильтровать") {
            eventList = eventList.filter {
                it.category == category
            }
        }
        if (date != "Не фильтровать"){
            val calendar = Calendar.getInstance()
            when(date){
                "Сегодня" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                    }
                }
                "На этой неделе" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH))
                    }
                }
                "В этом месяце" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    }
                }
            }
        }
        if(isNeedEquip){
            eventList = eventList.filter {it.isNeedEquip}
        }
        currentPlayersCount?.let {
            eventList = eventList.filter { event-> event.players!!.size == currentPlayersCount }
        }
        allPlayersCount?.let {
            eventList = eventList.filter { event-> event.playersCount== allPlayersCount }
        }
        Resource.Success(eventList)
    }

    suspend fun getFilteredListByUserId(
        id: String,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?
    ) = safeCall {
        val response = getAllEventsByUserId(id)
        if (response is Resource.Error){
            return@safeCall response
        }
        var eventList = response.data!!
        if (category != "Не фильтровать") {
            eventList = eventList.filter {
                it.category == category
            }
        }
        if (date != "Не фильтровать"){
            val calendar = Calendar.getInstance()
            when(date){
                "Сегодня" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                    }
                }
                "На этой неделе" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH))
                    }
                }
                "В этом месяце" ->{
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    }
                }
            }
        }
        if(isNeedEquip){
            eventList = eventList.filter {it.isNeedEquip}
        }
        currentPlayersCount?.let {
            eventList = eventList.filter { event-> event.players!!.size == currentPlayersCount }
        }
        allPlayersCount?.let {
            eventList = eventList.filter { event-> event.playersCount== allPlayersCount }
        }
        Resource.Success(eventList)
    }
}