package com.zolax.zevent.util

import com.zolax.zevent.models.Player
import com.zolax.zevent.models.Score

object HungarianAlgorithm {
    private fun findLargest //Finds the largest element in a positive array.
                (array: Array<DoubleArray>): Double //works for arrays where all values are >= 0.
    {
        var largest = 0.0
        for (i in array.indices) {
            for (j in array[i].indices) {
                if (array[i][j] > largest) {
                    largest = array[i][j]
                }
            }
        }
        return largest
    }

    fun hgAlgorithm(array: Array<DoubleArray>, sumType: String): Array<IntArray> {
        val cost = array.clone() //Create the cost matrix
        if (sumType.equals(
                "max",
                ignoreCase = true
            )
        ) //Then array is weight array. Must change to cost.
        {
            val maxWeight = findLargest(cost)
            for (i in cost.indices)  //Generate cost by subtracting.
            {
                for (j in cost[i].indices) {
                    cost[i][j] = maxWeight - cost[i][j]
                }
            }
        }
        val maxCost = findLargest(cost) //Find largest cost matrix element (needed for step 6).
        val mask = Array(cost.size) { IntArray(cost[0].size) } //The mask array.
        val rowCover = IntArray(cost.size) //The row covering vector.
        val colCover = IntArray(cost[0].size) //The column covering vector.
        val zeroRc = IntArray(2) //Position of last zero from Step 4.
        var step = 1
        var done = false
        while (!done) //main execution loop
        {
            when (step) {
                1 -> step = hgStep1(step, cost)
                2 -> step = hgStep2(step, cost, mask, rowCover, colCover)
                3 -> step = hgStep3(step, mask, colCover)
                4 -> step = hgStep4(step, cost, mask, rowCover, colCover, zeroRc)
                5 -> step = hgStep5(step, mask, rowCover, colCover, zeroRc)
                6 -> step = hgStep6(step, cost, rowCover, colCover, maxCost)
                7 -> done = true
            }
        } //end while
        val assignment = Array(array.size) { IntArray(2) } //Create the returned array.
        for (i in mask.indices) {
            for (j in mask[i].indices) {
                if (mask[i][j] == 1) {
                    assignment[i][0] = i
                    assignment[i][1] = j
                }
            }
        }
        return assignment
    }

    private fun hgStep1(step: Int, cost: Array<DoubleArray>): Int {
        //What STEP 1 does:
        //For each row of the cost matrix, find the smallest element
        //and subtract it from from every other element in its row.
        var step = step
        var minval: Double
        for (i in cost.indices) {
            minval = cost[i][0]
            for (j in cost[i].indices)  //1st inner loop finds min val in row.
            {
                if (minval > cost[i][j]) {
                    minval = cost[i][j]
                }
            }
            for (j in cost[i].indices)  //2nd inner loop subtracts it.
            {
                cost[i][j] = cost[i][j] - minval
            }
        }
        step = 2
        return step
    }

    private fun hgStep2(
        step: Int,
        cost: Array<DoubleArray>,
        mask: Array<IntArray>,
        rowCover: IntArray,
        colCover: IntArray
    ): Int {
        //What STEP 2 does:
        //Marks uncovered zeros as starred and covers their row and column.
        var step = step
        for (i in cost.indices) {
            for (j in cost[i].indices) {
                if (cost[i][j] == 0.0 && colCover[j] == 0 && rowCover[i] == 0) {
                    mask[i][j] = 1
                    colCover[j] = 1
                    rowCover[i] = 1
                }
            }
        }
        clearCovers(rowCover, colCover) //Reset cover vectors.
        step = 3
        return step
    }

    private fun hgStep3(step: Int, mask: Array<IntArray>, colCover: IntArray): Int {
        //What STEP 3 does:
        //Cover columns of starred zeros. Check if all columns are covered.
        var step = step
        for (i in mask.indices)  //Cover columns of starred zeros.
        {
            for (j in mask[i].indices) {
                if (mask[i][j] == 1) {
                    colCover[j] = 1
                }
            }
        }
        var count = 0
        for (j in colCover.indices)  //Check if all columns are covered.
        {
            count += colCover[j]
        }
        step =
            if (count >= mask.size) //Should be cost.length but ok, because mask has same dimensions.
            {
                7
            } else {
                4
            }
        return step
    }

    private fun hgStep4(
        step: Int,
        cost: Array<DoubleArray>,
        mask: Array<IntArray>,
        rowCover: IntArray,
        colCover: IntArray,
        zero_RC: IntArray
    ): Int {
        //What STEP 4 does:
        //Find an uncovered zero in cost and prime it (if none go to step 6). Check for star in same row:
        //if yes, cover the row and uncover the star's column. Repeat until no uncovered zeros are left
        //and go to step 6. If not, save location of primed zero and go to step 5.
        var step = step
        var rowCol = IntArray(2) //Holds row and col of uncovered zero.
        var done = false
        while (!done) {
            rowCol = findUncoveredZero(rowCol, cost, rowCover, colCover)
            if (rowCol[0] == -1) {
                done = true
                step = 6
            } else {
                mask[rowCol[0]][rowCol[1]] = 2 //Prime the found uncovered zero.
                var starInRow = false
                for (j in mask[rowCol[0]].indices) {
                    if (mask[rowCol[0]][j] == 1) //If there is a star in the same row...
                    {
                        starInRow = true
                        rowCol[1] = j //remember its column.
                    }
                }
                if (starInRow) {
                    rowCover[rowCol[0]] = 1 //Cover the star's row.
                    colCover[rowCol[1]] = 0 //Uncover its column.
                } else {
                    zero_RC[0] = rowCol[0] //Save row of primed zero.
                    zero_RC[1] = rowCol[1] //Save column of primed zero.
                    done = true
                    step = 5
                }
            }
        }
        return step
    }

    private fun findUncoveredZero //Aux 1 for hg_step4.
                (
        row_col: IntArray,
        cost: Array<DoubleArray>,
        rowCover: IntArray,
        colCover: IntArray
    ): IntArray {
        row_col[0] = -1 //Just a check value. Not a real index.
        row_col[1] = 0
        var i = 0
        var done = false
        while (!done) {
            var j = 0
            while (j < cost[i].size) {
                if (cost[i][j] == 0.0 && rowCover[i] == 0 && colCover[j] == 0) {
                    row_col[0] = i
                    row_col[1] = j
                    done = true
                }
                j += 1
            } //end inner while
            i += 1
            if (i >= cost.size) {
                done = true
            }
        } //end outer while
        return row_col
    }

    private fun hgStep5(
        step: Int,
        mask: Array<IntArray>,
        rowCover: IntArray,
        colCover: IntArray,
        zero_RC: IntArray
    ): Int {
        //What STEP 5 does:
        //Construct series of alternating primes and stars. Start with prime from step 4.
        //Take star in the same column. Next take prime in the same row as the star. Finish
        //at a prime with no star in its column. Unstar all stars and star the primes of the
        //series. Erasy any other primes. Reset covers. Go to step 3.
        var step = step
        var count = 0 //Counts rows of the path matrix.
        val path =
            Array(mask[0].size * mask.size) { IntArray(2) } //Path matrix (stores row and col).
        path[count][0] = zero_RC[0] //Row of last prime.
        path[count][1] = zero_RC[1] //Column of last prime.
        var done = false
        while (!done) {
            val r = findStarInCol(mask, path[count][1])
            if (r >= 0) {
                count += 1
                path[count][0] = r //Row of starred zero.
                path[count][1] = path[count - 1][1] //Column of starred zero.
            } else {
                done = true
            }
            if (!done) {
                val c = findPrimeInRow(mask, path[count][0])
                count += 1
                path[count][0] = path[count - 1][0] //Row of primed zero.
                path[count][1] = c //Col of primed zero.
            }
        } //end while
        convertPath(mask, path, count)
        clearCovers(rowCover, colCover)
        erasePrimes(mask)
        step = 3
        return step
    }

    private fun findStarInCol //Aux 1 for hg_step5.
                (mask: Array<IntArray>, col: Int): Int {
        var r = -1 //Again this is a check value.
        for (i in mask.indices) {
            if (mask[i][col] == 1) {
                r = i
            }
        }
        return r
    }

    private fun findPrimeInRow //Aux 2 for hg_step5.
                (mask: Array<IntArray>, row: Int): Int {
        var c = -1
        for (j in mask[row].indices) {
            if (mask[row][j] == 2) {
                c = j
            }
        }
        return c
    }

    private fun convertPath //Aux 3 for hg_step5.
                (mask: Array<IntArray>, path: Array<IntArray>, count: Int) {
        for (i in 0..count) {
            if (mask[path[i][0]][path[i][1]] == 1) {
                mask[path[i][0]][path[i][1]] = 0
            } else {
                mask[path[i][0]][path[i][1]] = 1
            }
        }
    }

    private fun erasePrimes //Aux 4 for hg_step5.
                (mask: Array<IntArray>) {
        for (i in mask.indices) {
            for (j in mask[i].indices) {
                if (mask[i][j] == 2) {
                    mask[i][j] = 0
                }
            }
        }
    }

    private fun clearCovers //Aux 5 for hg_step5 (and not only).
                (rowCover: IntArray, colCover: IntArray) {
        for (i in rowCover.indices) {
            rowCover[i] = 0
        }
        for (j in colCover.indices) {
            colCover[j] = 0
        }
    }

    private fun hgStep6(
        step: Int,
        cost: Array<DoubleArray>,
        rowCover: IntArray,
        colCover: IntArray,
        maxCost: Double
    ): Int {
        //What STEP 6 does:
        //Find smallest uncovered value in cost: a. Add it to every element of covered rows
        //b. Subtract it from every element of uncovered columns. Go to step 4.
        var step = step
        val minval = findSmallest(cost, rowCover, colCover, maxCost)
        for (i in rowCover.indices) {
            for (j in colCover.indices) {
                if (rowCover[i] == 1) {
                    cost[i][j] = cost[i][j] + minval
                }
                if (colCover[j] == 0) {
                    cost[i][j] = cost[i][j] - minval
                }
            }
        }
        step = 4
        return step
    }

    private fun findSmallest //Aux 1 for hg_step6.
                (
        cost: Array<DoubleArray>,
        rowCover: IntArray,
        colCover: IntArray,
        maxCost: Double
    ): Double {
        var minval = maxCost //There cannot be a larger cost than this.
        for (i in cost.indices)  //Now find the smallest uncovered value.
        {
            for (j in cost[i].indices) {
                if (rowCover[i] == 0 && colCover[j] == 0 && minval > cost[i][j]) {
                    minval = cost[i][j]
                }
            }
        }
        return minval
    }

    fun eventPlayersRecomendation(
        data: List<Pair<Player, Score>>,
        category: String
    ): MutableList<Player>? {
        when (category) {
            "Футбол" -> {
                val diff = (22 - data.size)
                val result = createFootballMatrix(data)
                val hungarianMatrix = hgAlgorithm(result.first, "max")
                val roles = createFootballRolesArray(result.second)
                val playersWithRoles = mutableListOf<Player>()
                val mutableData = mutableListOf<Pair<Player, Score>>()

                data.forEach { player ->
                    if (player.first.role == "Рекомендуемое") {
                        mutableData.add(player)
                    }
                }

                for (i in diff until hungarianMatrix.size) {
                    val player = mutableData[hungarianMatrix[i - diff][0]].first
                    player.role = roles[hungarianMatrix[i][1]]
                    playersWithRoles.add(player)
                }

                val resultList = mutableListOf<Player>()
                var index = 0

                for (i in data.indices) {
                    if (data[i].first.role == "Рекомендуемое") {
                        resultList.add(playersWithRoles[i])
                        index++
                    } else {
                        resultList.add(data[i].first)
                    }

                }

                return resultList
            }
            "Баскетбол" -> {
                val diff = (10 - data.size)
                val result = createBasketballMatrix(data)
                val hungarianMatrix = hgAlgorithm(result.first, "max")
                val roles = createBasketballRolesArray(result.second)
                val playersWithRoles = mutableListOf<Player>()
                val mutableData = mutableListOf<Pair<Player, Score>>()

                data.forEach { player ->
                    if (player.first.role == "Рекомендуемое") {
                        mutableData.add(player)
                    }
                }

                for (i in diff until hungarianMatrix.size) {
                    val player = mutableData[hungarianMatrix[i - diff][0]].first
                    player.role = roles[hungarianMatrix[i][1]]
                    playersWithRoles.add(player)
                }

                val resultList = mutableListOf<Player>()
                var index = 0

                for (i in data.indices) {
                    if (data[i].first.role == "Рекомендуемое") {
                        resultList.add(playersWithRoles[i])
                        index++
                    } else {
                        resultList.add(data[i].first)
                    }

                }

                return resultList

            }
            "Волейбол" -> {
                val diff = (12 - data.size)
                val result = createVolleyballMatrix(data)
                val hungarianMatrix = hgAlgorithm(result.first, "max")
                val roles = createVolleyballRolesArray(result.second)
                val playersWithRoles = mutableListOf<Player>()
                val mutableData = mutableListOf<Pair<Player, Score>>()

                data.forEach { player ->
                    if (player.first.role == "Рекомендуемое") {
                        mutableData.add(player)
                    }
                }

                for (i in diff until hungarianMatrix.size) {
                    val player = mutableData[hungarianMatrix[i - diff][0]].first
                    player.role = roles[hungarianMatrix[i][1]]
                    playersWithRoles.add(player)
                }

                val resultList = mutableListOf<Player>()
                var index = 0

                for (i in data.indices) {
                    if (data[i].first.role == "Рекомендуемое") {
                        resultList.add(playersWithRoles[i])
                        index++
                    } else {
                        resultList.add(data[i].first)
                    }

                }

                return resultList

            }
        }
        return null
    }

    private fun createVolleyballRolesArray(roles: MutableMap<String, Int>): MutableList<String> {
        val result = mutableListOf<String>()
        if (roles["Либеро"]!! > 0) {
            for (i in 1..roles["Либеро"]!!) {
                result.add("Либеро")
            }
        }
        if (roles["Диагональный"]!! > 0) {
            for (i in 1..roles["Диагональный"]!!) {
                result.add("Диагональный")
            }
        }
        if (roles["Доигровщик"]!! > 0) {
            for (i in 1..roles["Доигровщик"]!!) {
                result.add("Доигровщик")
            }
        }
        if (roles["Центральный блокирующий"]!! > 0) {
            for (i in 1..roles["Центральный блокирующий"]!!) {
                result.add("Центральный блокирующий")
            }
        }
        if (roles["Связующий"]!! > 0) {
            for (i in 1..roles["Связующий"]!!) {
                result.add("Связующий")
            }
        }
        if (roles["Подающий"]!! > 0) {
            for (i in 1..roles["Подающий"]!!) {
                result.add("Подающий")
            }
        }
        return result
    }

    private fun createVolleyballMatrix(data: List<Pair<Player, Score>>): Pair<Array<DoubleArray>, MutableMap<String, Int>> {
        var neededRoles = 12
        val roles = mutableMapOf(
            Pair("Либеро", 2),
            Pair("Диагональный", 2),
            Pair("Доигровщик", 2),
            Pair("Центральный блокирующий", 2),
            Pair("Связующий", 2),
            Pair("Подающий", 2)
        )
        val mutableData = mutableListOf<Pair<Player, Score>>()
        data.forEach { player ->
            if (player.first.role != "Рекомендуемое") {
                roles[player.first.role!!] = roles[player.first.role]!! - 1
                neededRoles--
            }
        }
        data.forEach { player ->
            if (player.first.role == "Рекомендуемое") {
                mutableData.add(player)
            }
        }

        val matrix = Array(neededRoles) { DoubleArray(neededRoles) { 0.0 } }

        val allRolesCount = 12
        var allPlayersCount = data.size

        var row = allRolesCount - allPlayersCount
        var column = 0

        mutableData.forEach {
            if (roles["Либеро"]!! > 0) {
                for (i in 1..roles["Либеро"]!!) {
                    matrix[row][column] = it.second.scores!!["Либеро"]!!.toDouble()
                    column++
                }
            }
            if (roles["Диагональный"]!! > 0) {
                for (i in 1..roles["Диагональный"]!!) {
                    matrix[row][column] = it.second.scores!!["Диагональный"]!!.toDouble()
                    column++
                }
            }
            if (roles["Доигровщик"]!! > 0) {
                for (i in 1..roles["Доигровщик"]!!) {
                    matrix[row][column] = it.second.scores!!["Доигровщик"]!!.toDouble()
                    column++
                }
            }
            if (roles["Центральный блокирующий"]!! > 0) {
                for (i in 1..roles["Центральный блокирующий"]!!) {
                    matrix[row][column] = it.second.scores!!["Центральный блокирующий"]!!.toDouble()
                    column++
                }
            }
            if (roles["Связующий"]!! > 0) {
                for (i in 1..roles["Связующий"]!!) {
                    matrix[row][column] = it.second.scores!!["Связующий"]!!.toDouble()
                    column++
                }
            }
            if (roles["Подающий"]!! > 0) {
                for (i in 1..roles["Подающий"]!!) {
                    matrix[row][column] = it.second.scores!!["Подающий"]!!.toDouble()
                    column++
                }
            }
            row++
            column = 0
        }

        return Pair(matrix, roles)
    }

    private fun createBasketballRolesArray(roles: MutableMap<String, Int>): MutableList<String> {
        val result = mutableListOf<String>()
        if (roles["Защитник"]!! > 0) {
            for (i in 1..roles["Защитник"]!!) {
                result.add("Защитник")
            }
        }
        if (roles["Форвард"]!! > 0) {
            for (i in 1..roles["Форвард"]!!) {
                result.add("Форвард")
            }
        }
        if (roles["Центровой"]!! > 0) {
            for (i in 1..roles["Центровой"]!!) {
                result.add("Центровой")
            }
        }
        return result
    }

    private fun createBasketballMatrix(data: List<Pair<Player, Score>>): Pair<Array<DoubleArray>, MutableMap<String, Int>> {
        var neededRoles = 10
        val roles = mutableMapOf(
            Pair("Защитник", 4),
            Pair("Форвард", 4),
            Pair("Центровой", 2),
        )
        val mutableData = mutableListOf<Pair<Player, Score>>()
        data.forEach { player ->
            if (player.first.role != "Рекомендуемое") {
                roles[player.first.role!!] = roles[player.first.role]!! - 1
                neededRoles--
            }
        }
        data.forEach { player ->
            if (player.first.role == "Рекомендуемое") {
                mutableData.add(player)
            }
        }


        val matrix = Array(neededRoles) { DoubleArray(neededRoles) { 0.0 } }


        val allRolesCount = 10
        var allPlayersCount = data.size

        var row = allRolesCount - allPlayersCount
        var column = 0



        mutableData.forEach {
            if (roles["Защитник"]!! > 0) {
                for (i in 1..roles["Защитник"]!!) {
                    matrix[row][column] = it.second.scores!!["Защитник"]!!.toDouble()
                    column++
                }
            }
            if (roles["Форвард"]!! > 0) {
                for (i in 1..roles["Форвард"]!!) {
                    matrix[row][column] = it.second.scores!!["Форвард"]!!.toDouble()
                    column++
                }
            }
            if (roles["Центровой"]!! > 0) {
                for (i in 1..roles["Центровой"]!!) {
                    matrix[row][column] = it.second.scores!!["Центровой"]!!.toDouble()
                    column++
                }
            }
            row++
            column = 0
        }

        return Pair(matrix, roles)
    }

    private fun createFootballRolesArray(roles: MutableMap<String, Int>): MutableList<String> {
        val result = mutableListOf<String>()
        if (roles["Нападающий"]!! > 0) {
            for (i in 1..roles["Нападающий"]!!) {
                result.add("Нападающий")
            }
        }
        if (roles["Полузащитник"]!! > 0) {
            for (i in 1..roles["Полузащитник"]!!) {
                result.add("Полузащитник")
            }
        }
        if (roles["Защитник"]!! > 0) {
            for (i in 1..roles["Защитник"]!!) {
                result.add("Защитник")
            }
        }
        if (roles["Вратарь"]!! > 0) {
            for (i in 1..roles["Вратарь"]!!) {
                result.add("Вратарь")
            }
        }
        return result
    }


    private fun createFootballMatrix(data: List<Pair<Player, Score>>): Pair<Array<DoubleArray>, MutableMap<String, Int>> {

        var neededRoles = 22
        val roles = mutableMapOf(
            Pair("Нападающий", 6),
            Pair("Полузащитник", 8),
            Pair("Защитник", 6),
            Pair("Вратарь", 2)
        )
        val mutableData = mutableListOf<Pair<Player, Score>>()
        data.forEach { player ->
            if (player.first.role != "Рекомендуемое") {
                roles[player.first.role!!] = roles[player.first.role]!! - 1
                neededRoles--
            }
        }
        data.forEach { player ->
            if (player.first.role == "Рекомендуемое") {
                mutableData.add(player)
            }
        }


        val matrix = Array(neededRoles) { DoubleArray(neededRoles) { 0.0 } }


        val allRolesCount = 22
        var allPlayersCount = data.size

        var row = allRolesCount - allPlayersCount
        var column = 0



        mutableData.forEach {
            if (roles["Нападающий"]!! > 0) {
                for (i in 1..roles["Нападающий"]!!) {
                    matrix[row][column] = it.second.scores!!["Нападающий"]!!.toDouble()
                    column++
                }
            }
            if (roles["Полузащитник"]!! > 0) {
                for (i in 1..roles["Полузащитник"]!!) {
                    matrix[row][column] = it.second.scores!!["Полузащитник"]!!.toDouble()
                    column++
                }
            }
            if (roles["Защитник"]!! > 0) {
                for (i in 1..roles["Защитник"]!!) {
                    matrix[row][column] = it.second.scores!!["Защитник"]!!.toDouble()
                    column++
                }
            }
            if (roles["Вратарь"]!! > 0) {
                for (i in 1..roles["Вратарь"]!!) {
                    matrix[row][column] = it.second.scores!!["Вратарь"]!!.toDouble()
                    column++
                }
            }
            row++
            column = 0
        }

        return Pair(matrix, roles)
    }

}







