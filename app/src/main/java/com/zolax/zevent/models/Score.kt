package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId
import com.google.type.DateTime
import com.google.type.LatLng

data class Score(
    @DocumentId
    var id: String? = null,
    var sportType:Int = OTHER,
    var scores: Map<String,Int> = mapOf()
){
    companion object{
        const val FOOTBAL: Int = 1
        const val BASKETBALL: Int = 2
        const val VOLLEYBAL: Int = 3
        const val OTHER: Int = 4
    }
}
