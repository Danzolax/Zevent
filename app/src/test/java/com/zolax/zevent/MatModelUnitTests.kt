package com.zolax.zevent

import com.google.android.gms.maps.model.LatLng
import com.zolax.zevent.models.Player
import com.zolax.zevent.models.Score
import com.zolax.zevent.util.HungarianAlgorithm
import com.zolax.zevent.util.LocationRecommend
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MatModelUnitTests {
    @Test
    fun locationRecommendCorrect() {
        val players = arrayListOf(
            Player("a", "Защитник", "Новичок", 55.765059, 37.572683),
            Player("s", "Нападающий", "Любитель", 55.774380, 37.586440),
            Player("d", "Вратарь", "Новичок", 55.776040, 37.574760)
        )
        assertEquals(LatLng(55.77604, 37.57476), LocationRecommend.getRecommendLocation(players))
    }

    @Test
    fun hungarianAlgorithmCorrect() {
        val data = arrayListOf(
            Pair(
                Player("a", "Рекомендуемое", "Новичок", 55.765059, 37.572683),
                Score(
                    "id1",
                    "a",
                    Score.FOOTBALL,
                    mutableMapOf(
                        "Вратарь" to 70,
                        "Защитник" to 80,
                        "Нападающий" to 90,
                        "Полузащитник" to 100,
                    )
                )
            ),
            Pair(
                Player("b", "Рекомендуемое", "Новичок", 55.765059, 37.572683),
                Score(
                    "id2",
                    "b",
                    Score.FOOTBALL,
                    mutableMapOf(
                        "Вратарь" to 100,
                        "Защитник" to 90,
                        "Нападающий" to 80,
                        "Полузащитник" to 70,
                    )
                )
            ),
            Pair(
                Player("c", "Рекомендуемое", "Новичок", 55.765059, 37.572683),
                Score(
                    "id3",
                    "c",
                    Score.FOOTBALL,
                    mutableMapOf(
                        "Вратарь" to 80,
                        "Защитник" to 100,
                        "Нападающий" to 90,
                        "Полузащитник" to 70,
                    )
                )
            ),

        )
        val correctData = mutableListOf(
            Player("a", "Полузащитник", "Новичок", 55.765059, 37.572683),
            Player("b", "Вратарь", "Новичок", 55.765059, 37.572683),
            Player("c", "Защитник", "Новичок", 55.765059, 37.572683),
        )
        assertEquals(correctData,HungarianAlgorithm.eventPlayersRecomendation(data,"Футбол"))
    }
}