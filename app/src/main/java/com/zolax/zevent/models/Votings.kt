package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId

data class Votings(
    @DocumentId
    var id: String? = null,
    var userId: String? = null,
    var votings: MutableList<Voting>? = null
)
