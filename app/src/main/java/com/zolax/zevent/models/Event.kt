package com.zolax.zevent.models

import com.google.firebase.firestore.DocumentId
import com.google.type.DateTime
import com.google.type.LatLng

data class Event(
    @DocumentId
    var id: String? = null ,
    var title: String? = null,
    var category: String? = null,
    var playersCount:Int? = null,
    var eventDateTime: DateTime? = null,
    var isNeedEquip: Boolean = false,
    var needEquip: List<String> = arrayListOf(),
    var players: List<Player> = arrayListOf(),
    var location: LatLng? = null,

)
