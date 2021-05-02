package com.zolax.zevent.util

import com.google.android.gms.maps.model.LatLng
import com.zolax.zevent.models.Player
import java.lang.Double.min
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object LocationRecommend {
    private fun floid(matrix: Array<Array<Double>>){
        for (k in matrix.indices) for (i in matrix.indices) for (j in matrix.indices){
            matrix[i][j] = min(matrix[i][j], matrix[i][k] + matrix[k][j])
        }
    }

    private fun findCenter(table: Array<Array<Double>>): Int {
        val size = table.size
        val e: Array<Double> = Array(size) { 0.0 }
        for (i in table.indices){
            for (j in table.indices){
                e[i] = e[i] + table[i][j]
            }
        }
        return e.indexOf(e.min())
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

    fun getRecommendLocation(players: ArrayList<Player>) : LatLng{
        val array  = arrayListOf<Triple<Player,Player,Double>>()
        for (i in players.indices) {
            for (j in players.indices){
                array.add(Triple(players[i],players[j], distance(players[i].latitude!!,players[i].longitude!!,players[j].latitude!!,players[j].longitude!!)))
            }
        }
        val size = players.size
        val table: Array<Array<Double>> = Array(size) { Array(size) { 0.0 } }
        var n = 0
        for (i in players.indices){
            for (j in players.indices){
                table[i][j] = array[n].third
                n++
            }
        }
        floid(table)
        return LatLng(players[findCenter(table)].latitude!!,players[findCenter(table)].longitude!!)
    }
}