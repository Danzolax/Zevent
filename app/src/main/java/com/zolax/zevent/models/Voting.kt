package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Voting(
    var eventTitle: String? = null,
    var eventCategory: String? = null,
    var player: Player? = null,
)
