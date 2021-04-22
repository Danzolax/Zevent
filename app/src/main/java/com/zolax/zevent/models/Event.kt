package com.zolax.zevent.models

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import java.util.*

data class Event(
    @DocumentId
    var id: String? = null,
    var title: String? = null,
    var category: String? = null,
    var playersCount:Int? = null,
    var eventDateTime: Date? = null,
    @field:JvmField
    var isNeedEquip: Boolean = false,
    var needEquip: String?= null,
    var players: List<Player>? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,

    )
