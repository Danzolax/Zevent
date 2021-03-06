package com.zolax.zevent.repositories

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.zolax.zevent.models.*
import com.zolax.zevent.util.HungarianAlgorithm
import com.zolax.zevent.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
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
    private val beginEvents = Firebase.firestore.collection("beginEvents")
    private val votings = Firebase.firestore.collection("votings")
    private val scores = Firebase.firestore.collection("scores")

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
                createVoting(firebaseUser.uid)
                createScore(firebaseUser.uid)
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

    suspend fun downloadUserImage(id: String) = storageRef.child(id).downloadUrl.await()


    //???????????????????? uri ???????????????????? ????????????????????????
    suspend fun downloadUserImageById(id: String) =
        safeCall {
            Resource.Success(downloadUserImage(id))
        }


    suspend fun addEvent(event: Event) =
        safeCall {
            events.add(event).await()
            Resource.Success(event)
        }

    suspend fun getAllEvents() = safeCall {
        val eventsList = events.get().await().toObjects(Event::class.java)
        Resource.Success(eventsList)
    }

    //???????????????????? ???????????? ?????????????????????? ?????????????????????? ????????????????????????
    suspend fun getAllEventsByUserId(id: String) = safeCall {
        val eventsList = events.get().await().toObjects(Event::class.java)

        eventsList.removeIf {
            it.players?.find { it.userId == id } == null
        }
        Timber.d("$eventsList")
        Resource.Success(eventsList)
    }

    //???????????????????? ???????????? ?????????????????????? ???????? ?????????????????????????? ?????????? ????????????????
    suspend fun getAllEventsReverseByUserId(id: String) = safeCall {
        val eventsList = events.get().await().toObjects(Event::class.java)
        eventsList.removeIf {
            var flag = false
            it.players?.forEach { elem ->
                if (elem.userId.equals(id)) {
                    flag = true
                    return@forEach
                }
            }
            return@removeIf flag
        }
        eventsList.removeIf {
            it.players?.size == it.playersCount
        }
        Resource.Success(eventsList)
    }

    //???????????????????? ???????????? ?????????????????????? ???????? ?????????????????????????? ?????????? ???????????????? ?? ?????????????? 5 ????,
    suspend fun getAllEventsReverseByUserIdWithRadius(
        id: String,
        userLocation: LatLng,
        radius: Int
    ) = safeCall {
        val eventsList = events.get().await().toObjects(Event::class.java)
        eventsList.removeIf {
            var flag = false
            it.players?.forEach { elem ->
                if (elem.userId.equals(id)) {
                    flag = true
                    return@forEach
                }
            }
            return@removeIf flag
        }
        eventsList.removeIf {
            it.players?.size == it.playersCount
        }
        eventsList.removeIf {
            distance(
                userLocation.latitude,
                userLocation.longitude,
                it.latitude!!,
                it.longitude!!,
            ) > radius
        }
        Resource.Success(eventsList)
    }

    suspend fun getAllEventsWithRadius(userLocation: LatLng, radius: Int) = safeCall {
        val eventsList = events.get().await().toObjects(Event::class.java)
        eventsList.removeIf {
            it.players?.size == it.playersCount
        }
        eventsList.removeIf {
            distance(
                userLocation.latitude,
                userLocation.longitude,
                it.latitude!!,
                it.longitude!!,
            ) > radius
        }
        Resource.Success(eventsList)
    }

    //???????????????????? ???????????????????? ?????????? ?????????? ?????????????? ?? ????????????
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
        when (event!!.category) {
            "????????????" -> {
                var count = 0
                event.players!!.forEach {
                    if (it.role == player.role) {
                        count++
                    }
                }
                when (player.role) {
                    "????????????????????" -> {
                        if (count >= 6)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????????????" -> {
                        if (count >= 8)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????" -> {
                        if (count >= 6)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "??????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                }
            }
            "??????????????????" -> {
                var count = 0
                event.players!!.forEach {
                    if (it.role == player.role) {
                        count++
                    }
                }
                when (player.role) {
                    "??????????????" -> {
                        if (count >= 4)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????" -> {
                        if (count >= 4)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "??????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }

                }

            }
            "????????????????" -> {
                var count = 0
                event.players!!.forEach {
                    if (it.role == player.role) {
                        count++
                    }
                }
                when (player.role) {
                    "????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "?????????????????????? ??????????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "??????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                    "????????????????" -> {
                        if (count >= 2)
                            return@safeCall Resource.Error(msg = "???????? ????????????!")
                    }
                }
            }
        }
        val userEvents = getAllEventsByUserId(player.userId!!).data as MutableList
        Timber.d("User Events: ${userEvents}")
        val currentEvent = getEventById(id).data!!
        Timber.d("Current Event: ${currentEvent}")

        val findEvent = userEvents.find {
            val findEventDate = Calendar.getInstance()
            findEventDate.time = it.eventDateTime!!

            val currEventDate = Calendar.getInstance()
            currEventDate.time = currentEvent.eventDateTime!!

            return@find (findEventDate.get(Calendar.DAY_OF_YEAR) == currEventDate.get(Calendar.DAY_OF_YEAR)) and
                    (findEventDate.get(Calendar.HOUR_OF_DAY) == currEventDate.get(Calendar.HOUR_OF_DAY))
        }
        Timber.d("Find Event: ${findEvent}")

        if (findEvent != null) {
            return@safeCall Resource.Error(msg = "?????????????? ?????????? ?????????????????????? ?? ???????? ??????????")
        }
        (event.players as ArrayList).add(player)
        events.document(id).set(event).await()
        Resource.Success(event)
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

    //???????????????????? ???????????? ???? (????????????????????????,??????????,????????)
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

    suspend fun getBeginEventPlayersByEventId(id: String) = safeCall {
        val players: ArrayList<Triple<User, Player, Uri>> = arrayListOf()
        val event = beginEvents.document(id).get().await().toObject(Event::class.java)
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

    //???????????????????? ???????????? ?????????????????????? ???????? ?????????????????????????? ?????????? ???????????????? ?? ?????????????? 5 ????, ?? ???????????????????????? ?? ??????????????????
    suspend fun getFilteredListReverseByUserId(
        id: String,
        location: LatLng,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?,
        searchRadius: Int
    ) = safeCall {
        val response = getAllEventsReverseByUserIdWithRadius(id, location, searchRadius)
        if (response is Resource.Error) {
            return@safeCall response
        }
        var eventList = response.data!!
        if (category != "???? ??????????????????????") {
            eventList = eventList.filter {
                it.category == category
            }
        }
        if (date != "???? ??????????????????????") {
            val calendar = Calendar.getInstance()
            when (date) {
                "??????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                    }
                }
                "???? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH))
                    }
                }
                "?? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    }
                }
            }
        }
        if (isNeedEquip) {
            eventList = eventList.filter { it.isNeedEquip }
        }
        currentPlayersCount?.let {
            eventList = eventList.filter { event -> event.players!!.size == currentPlayersCount }
        }
        allPlayersCount?.let {
            eventList = eventList.filter { event -> event.playersCount == allPlayersCount }
        }
        Resource.Success(eventList)
    }

    suspend fun getFilteredEvents(
        location: LatLng,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?,
        searchRadius: Int
    ) = safeCall {
        val response = getAllEventsWithRadius(location, searchRadius)
        if (response is Resource.Error) {
            return@safeCall response
        }
        var eventList = response.data!!
        if (category != "???? ??????????????????????") {
            eventList = eventList.filter {
                it.category == category
            }
        }
        if (date != "???? ??????????????????????") {
            val calendar = Calendar.getInstance()
            when (date) {
                "??????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                    }
                }
                "???? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH))
                    }
                }
                "?? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    }
                }
            }
        }
        if (isNeedEquip) {
            eventList = eventList.filter { it.isNeedEquip }
        }
        currentPlayersCount?.let {
            eventList = eventList.filter { event -> event.players!!.size == currentPlayersCount }
        }
        allPlayersCount?.let {
            eventList = eventList.filter { event -> event.playersCount == allPlayersCount }
        }
        Resource.Success(eventList)
    }

    //???????????????????? ???????????? ?????????????????????? ?????????????????????? ????????????????????????, ?? ???????????????????????? ?? ??????????????????
    suspend fun getFilteredListByUserId(
        id: String,
        category: String,
        date: String,
        isNeedEquip: Boolean,
        currentPlayersCount: Int?,
        allPlayersCount: Int?
    ) = safeCall {
        val response = getAllEventsByUserId(id)
        if (response is Resource.Error) {
            return@safeCall response
        }
        var eventList = response.data!!
        if (category != "???? ??????????????????????") {
            eventList = eventList.filter {
                it.category == category
            }
        }
        if (date != "???? ??????????????????????") {
            val calendar = Calendar.getInstance()
            when (date) {
                "??????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
                    }
                }
                "???? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) and
                                (eventCalendar.get(Calendar.WEEK_OF_MONTH) == calendar.get(Calendar.WEEK_OF_MONTH))
                    }
                }
                "?? ???????? ????????????" -> {
                    eventList = eventList.filter {
                        val eventCalendar = Calendar.getInstance()
                        eventCalendar.time = it.eventDateTime!!
                        (eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) and
                                (eventCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH))
                    }
                }

            }
        }
        if (isNeedEquip) {
            eventList = eventList.filter { it.isNeedEquip }
        }
        currentPlayersCount?.let {
            eventList = eventList.filter { event -> event.players!!.size == currentPlayersCount }
        }
        allPlayersCount?.let {
            eventList = eventList.filter { event -> event.playersCount == allPlayersCount }
        }
        Resource.Success(eventList)
    }

    //?????????????????????? ??????????????????????, ?????????????? ???????????????? ?? ???????????? ????????????
    suspend fun moveEventsToBeginByUserID(id: String) {
        val eventsList = getAllEvents().data

        eventsList!!.forEach { event ->
            if (event.eventDateTime!!.time < Calendar.getInstance().timeInMillis) {
                events.document(event.id!!).delete().await()
                if (event.category != "????????????") {
                    val players = event.players
                    val playerScoreList = getPlayerScoreList(players!!, event.category!!)
                    event.players = HungarianAlgorithm.eventPlayersRecomendation(
                        playerScoreList,
                        event.category!!
                    )
                }
                beginEvents.document(event.id!!).set(event).await()
            }
        }
    }

    private suspend fun getPlayerScoreList(
        players: List<Player>,
        category: String
    ): List<Pair<Player, Score>> {
        val playerScoreList = mutableListOf<Pair<Player, Score>>()
        players.forEach { player ->
            val scoreList =
                scores.whereEqualTo("userId", player.userId).get().await()
                    .toObjects(Score::class.java)
            scoreList.forEach { score ->
                if (score.sportType == category) {
                    playerScoreList.add(Pair(player, score))
                    return@forEach
                }
            }
        }
        return playerScoreList
    }

    suspend fun getAllBeginEventsByUserId(id: String) = safeCall {
        val events = beginEvents.get().await().toObjects(Event::class.java)
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
        Resource.Success(events)
    }

    suspend fun getBeginEventById(id: String) = safeCall {
        val event = beginEvents.document(id).get().await().toObject(Event::class.java)
        Resource.Success(event)
    }

    suspend fun unsubscribeBeginEventById(id: String, player: Player) = safeCall {
        val event = beginEvents.document(id).get().await().toObject(Event::class.java)
        (event?.players as ArrayList).remove(player)
        beginEvents.document(id).set(event).await()
        Resource.Success<Unit>()
    }

    suspend fun addPlayersInVotingsAndDeleteBeginEvent(beginEventId: String, userId: String) =
        safeCall {
            val event = beginEvents.document(beginEventId).get().await().toObject(Event::class.java)
            event!!.players!!.forEach { player1 ->
                event.players!!.forEach { player2 ->
                    if (player1.userId != player2.userId) {
                        val voting = votings.whereEqualTo("userId", player1.userId).get().await()
                            .toObjects(Votings::class.java)[0]
                        if (voting.votings == null) {
                            voting.votings = mutableListOf()

                        }
                        voting.votings!!.add(
                            Voting(
                                eventTitle = event.title,
                                event.category,
                                getUser(player2.userId!!).data!!.name,
                                player2
                            )
                        )
                        votings.document(voting.id!!).set(voting).await()
                    }
                }
            }
            beginEvents.document(beginEventId).delete().await()
            Resource.Success<Unit>()
        }

    suspend fun deleteBeginEvent(beginEventId: String, userId: String) =
        safeCall {
            beginEvents.document(beginEventId).delete().await()
            Resource.Success<Unit>()
        }

    suspend fun createVoting(userId: String) {
        votings.add(Votings(userId = userId, votings = mutableListOf())).await()
    }

    suspend fun checkVotingCount(userId: String) = safeCall {
        val voting =
            votings.whereEqualTo("userId", userId).get().await().toObjects(Votings::class.java)[0]
        if (voting.votings!!.size > 0) {
            return@safeCall Resource.Success(true)
        }
        return@safeCall Resource.Success(false)
    }

    suspend fun getVotings(userId: String) = safeCall {
        val voting =
            votings.whereEqualTo("userId", userId).get().await().toObjects(Votings::class.java)[0]
        Resource.Success(voting)
    }

    suspend fun createScore(userId: String) {
        val footballScore = Score(userId = userId, sportType = Score.FOOTBALL)
        footballScore.scores = mutableMapOf(
            Pair("????????????????????", 100),
            Pair("????????????????????????", 100),
            Pair("????????????????", 100),
            Pair("??????????????", 100)
        )
        val basketballScore = Score(userId = userId, sportType = Score.BASKETBALL)
        basketballScore.scores = mutableMapOf(
            Pair("????????????????", 100),
            Pair("??????????????", 100),
            Pair("??????????????????", 100)
        )
        val volleybalScore = Score(userId = userId, sportType = Score.VOLLEYBALL)
        volleybalScore.scores = mutableMapOf(
            Pair("????????????", 100),
            Pair("????????????????????????", 100),
            Pair("????????????????????", 100),
            Pair("?????????????????????? ??????????????????????", 100),
            Pair("??????????????????", 100),
            Pair("????????????????", 100)
        )
        scores.add(footballScore).await()
        scores.add(basketballScore).await()
        scores.add(volleybalScore).await()
    }

    suspend fun addScore(
        voting: Voting,
        votingsId: String
    ) = safeCall {
        val scoreList =
            scores.whereEqualTo("userId", voting.player!!.userId).get().await()
                .toObjects(Score::class.java)
        var score = Score()
        scoreList.forEach {
            if (it.sportType == voting.eventCategory) {
                score = it
                return@forEach
            }
        }
        score.scores!![voting.player!!.role!!] = score.scores!![voting.player!!.role!!]!! + 1
        scores.document(score.id!!).set(score)

        val votingList = votings.document(votingsId).get().await().toObject(Votings::class.java)
        votingList!!.votings!!.remove(voting)
        votings.document(votingsId).set(votingList).await()
        Resource.Success<Unit>()
    }

    suspend fun removeScore(
        voting: Voting,
        votingsId: String
    ) = safeCall {
        val scoreList =
            scores.whereEqualTo("userId", voting.player!!.userId).get().await()
                .toObjects(Score::class.java)
        var score = Score()
        scoreList.forEach {
            if (it.sportType == voting.eventCategory) {
                score = it
                return@forEach
            }
        }
        if (score.scores!![voting.player!!.role!!]!! != 0) {
            score.scores!![voting.player!!.role!!] = score.scores!![voting.player!!.role!!]!! - 1
            scores.document(score.id!!).set(score)

        }
        val votingList = votings.document(votingsId).get().await().toObject(Votings::class.java)
        votingList!!.votings!!.remove(voting)
        votings.document(votingsId).set(votingList).await()
        Resource.Success<Unit>()
    }


}