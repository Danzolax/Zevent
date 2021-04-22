package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId

data class Player(
    @DocumentId
    var id: String? = null,
    var role: String? = null,
    var rank: String,
    var userId: String
) {

}
