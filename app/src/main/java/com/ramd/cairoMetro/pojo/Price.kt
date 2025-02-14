package com.ramd.cairoMetro.pojo

class Price {

   fun calculatePrice(stationCount:Int): Int {

        return when {
            stationCount <= 9 -> 8
            stationCount <= 16 -> 10
            stationCount <= 23 -> 15
            else -> 20
        }

    }


}