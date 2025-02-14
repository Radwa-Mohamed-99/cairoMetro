package com.ramd.cairoMetro.pojo

import android.util.Log

class Direction( val data:Array<DataItem> ) {


    fun shortByIntersections(paths:List<List<String>>):List<String>{
        var min = 20
        var shortPath = emptyList<String>()
        for (path in paths) {
            val intersection = findIntersections(path)
            if( intersection.size < min)
            {
                min = intersection.size
                shortPath = path
            }
        }
        return shortPath
    }

    fun findLine (first:String,second:String ):String{
        var lineName =""
        val firstStation = data.filter { it.name == first }
        val secondStation = data.filter { it.name == second }

        for (dataItem1 in firstStation) {
            for (dataItem2 in secondStation) {
                if(dataItem2.line == dataItem1.line)
                {
                    lineName = dataItem2.line

                }
            }
        }
        return lineName
    }


    fun findIntersections(path:List<String>):MutableList<String>{
        val intersections = mutableListOf<String>()
        var lineName = findLine(path[0],path[1])
        val intersectionStations = data.filter { it.intersection }.map { it.name }.toSet()
        for (index in path.indices) {
            if((index + 1 )< path.size) {
                val nextStation = data.filter { it.name == path[index + 1] }.map { it.line }
                val station = path[index]
                if (station in intersectionStations && !nextStation.contains(lineName)) {
                    intersections.add(station)
                    lineName = findLine(station, path[index + 1])
                }
            }
        }
        return intersections
    }



}