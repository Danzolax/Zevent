package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId

data class Score(
    @DocumentId
    var id: String? = null,
    var userId: String? = null,
    var sportType: String? = null,
    var scores: MutableMap<String, Int>? = null,
) {
    companion object {
        const val FOOTBALL = "Футбол"
        const val BASKETBALL = "Баскетбол"
        const val VOLLEYBALL = "Воллейбол"
    }
}
