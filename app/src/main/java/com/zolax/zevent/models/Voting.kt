package com.zolax.zevent.models



data class Voting(
    var eventTitle: String? = null,
    var eventCategory: String? = null,
    var userName: String? = null,
    var player: Player? = null,
)
