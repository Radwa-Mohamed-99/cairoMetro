package com.ramd.cairoMetro.pojo

data class Direction(private val metroLines: MetroLines){
    fun direction(path:List<String>, lineSearch:List<List<String>>):String{

        val directionStart=directionSearch (path[1],path[0],lineSearch)
        var guide = "take ${directionStart[1]} in direction of ${directionStart[0]} ,"
        var line = metroLines.searchLine (path[1] ,path[0], lineSearch)
        var found = false


        for (index in path.indices) {
            val station = path[index]
            if (station in metroLines.intersectionStations && (index + 1 )< path.size && !lineSearch[line].contains(path[index + 1])) {
                guide += "intersection at: \"$station\","
                line = metroLines.searchLine(station, path[index + 1], lineSearch)
                found=true
            }
        }


        if (found) {
            val directionEnd = directionSearch(path[path.size - 1], path[path.size - 2], lineSearch)
            guide += "to ${directionEnd[1]} in direction of : ${directionEnd[0]} \n"
        }
        else {
            guide += "and there are no intersections \n"
        }
        return guide
    }
    private fun directionSearch (station1:String, station2:String, lineSearch: List<List<String>>): Array<String> {
        val directions = listOf(
            Pair("helwan", "new marg"),
            Pair("el monib", "shubra el khaimah"),
            Pair("rod el farag corridor", "adly mansour"),
            Pair("el monib", "rod el farag corridor")
        )

        val lineNames = listOf("line one", "line two", "line three", "line three")
        val directionAndLine = Array(2) { "" }

        for (i in lineSearch.indices) {
            if (station1 in lineSearch[i] && station2 in lineSearch[i]) {
                val station1Index = lineSearch[i].indexOf(station1)
                val station2Index = lineSearch[i].indexOf(station2)

                directionAndLine[1] = lineNames[i]
                directionAndLine[0] = if (station1Index > station2Index) directions[i].first else directions[i].second
                break
            }
        }

        return directionAndLine
    }
}