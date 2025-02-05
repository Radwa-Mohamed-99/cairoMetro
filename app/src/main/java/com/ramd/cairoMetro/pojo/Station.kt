package com.ramd.cairoMetro.pojo

data class Station(
    val name: String,
    val isIntersection: Boolean = false
) {
    val neighbors = mutableListOf<String>()
    fun addNeighbor(station: String) {
        neighbors.add(station)
    }
}
