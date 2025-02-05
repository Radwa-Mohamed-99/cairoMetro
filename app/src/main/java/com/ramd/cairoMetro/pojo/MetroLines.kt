package com.ramd.cairoMetro.pojo

open class MetroLines {
    val stations = mutableMapOf<String, Station>()
    val intersectionStations = listOf("sadat", "nasser", "orabi",  "ataba", "al shohadaa","kit kat", "cairo university")
    val lines = listOf(
        listOf(
            "new marg", "el marg", "ezbet el nakhl", "ain shams", "el matareyya",
            "helmeyet el zaitoun", "hadayeq el zaitoun", "saray el qobba", "hammamat el qobba",
            "kobri el qobba", "mansheiet el sadr", "el demerdash", "ghamra", "al shohadaa",
            "orabi", "nasser", "sadat", "saad zaghloul", "al sayeda zeinab", "el malek el saleh",
            "mar girgis", "el zahraa", "dar el salam", "hadayek el maadi", "maadi",
            "sakanat el maadi", "tora el balad", "kozzika", "tura el esmant", "el maasraa",
            "hadayek helwan", "wadi hof", "helwan university", "ain helwan", "helwan"
        ),
        listOf(
            "shubra el khaimah", "koliet el zeraa", "mezallat", "khalafawy", "st. teresa",
            "rod el farag", "masarra", "al shohadaa", "ataba", "mohamed naguib",
            "sadat", "opera", "dokki", "el bohooth", "cairo university", "faisal",
            "giza", "omm el masryeen", "saqiyet makky", "el monib"
        ),
        listOf(
            "adly mansour", "haykestep", "omar ibn el khattab", "qubaa", "hesham barakat",
            "el nozha", "nadi el shams", "alf maskan", "heliopolis square", "haroun",
            "al ahram", "koleyet el banat", "stadium", "fair zone", "abbasseya",
            "abdou pasha", "el geish", "bab el shaaria", "ataba", "nasser", "maspero",
            "safa hegazy", "kit kat", "sudan", "imbaba", "el bohy", "el qawmia", "ring road","rod el farag corridor"),
        listOf("kit kat", "tawfikia", "wadi el nile",
            "gamat el dowal", "boulak el dakrour", "cairo university")

    )
    init {
        makeMetroGraph()
    }
    private fun addEdge(station1: String, station2: String) {
        val isIntersection1 = station1 in intersectionStations
        val isIntersection2 = station2 in intersectionStations
        stations.putIfAbsent(station1,Station(station1, isIntersection = isIntersection1))
        stations.putIfAbsent(station2, Station(station2, isIntersection = isIntersection2))
        stations[station1]?.addNeighbor(station2)
        stations[station2]?.addNeighbor(station1)
    }
    private fun makeMetroGraph() {
        lines.forEach { line ->
            for (i in 0..<line.size-1) {
                addEdge(line[i], line[i + 1])
            }
        }
    }
    fun searchLine (first:String ,second:String, lineSearch: List<List<String>>):Int{
        for (i in lineSearch.indices) {
            if (first in lineSearch[i] && second in lineSearch[i]) {
                return i
            }
        }
        return -1
    }
}