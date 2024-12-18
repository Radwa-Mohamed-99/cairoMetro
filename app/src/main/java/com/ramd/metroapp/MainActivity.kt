package com.`as`.cairometro

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var startStationText: Spinner
    lateinit var endStationText: Spinner
    lateinit var shortPathText: TextView
    lateinit var countStationText:TextView
    lateinit var timeJourneyText :TextView
    lateinit var ticketPriceText:TextView
    lateinit var allPathText : TextView
    lateinit var directionText :TextView
    lateinit var findPathButton: Button
    lateinit var showAllPathsButton: Button
    var allPaths: List<List<String>> = emptyList()
    val graph = mutableMapOf<String, MutableList<String>>()
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.directionText)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startStationText = findViewById(R.id.startStationText)
        endStationText = findViewById(R.id.endStationText)
        shortPathText = findViewById(R.id.shortPathText)
        countStationText= findViewById(R.id.countStationText)
        timeJourneyText = findViewById(R.id.timeJourneyText)
        ticketPriceText = findViewById(R.id.ticketPriceText)
        allPathText = findViewById(R.id.allPathText)
        findPathButton = findViewById(R.id.findPathButton)
        showAllPathsButton = findViewById(R.id.showAllPathsButton)
        directionText = findViewById(R.id.directionText)
        val stationAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, lines.flatten())
        startStationText.adapter = stationAdapter
        endStationText.adapter = stationAdapter
        makeMetroGraph()
        showAllPathsButton.isEnabled=false
    }
    fun makeMetroGraph() {
        lines.forEach { line ->
            for (i in 0..<line.size-1) {
                addEdge(line[i], line[i + 1])
            }
        }
    }
    fun addEdge(station1: String, station2: String) {
        graph.putIfAbsent(station1, mutableListOf())
        graph.putIfAbsent(station2, mutableListOf())
        graph[station1]?.add(station2)
        graph[station2]?.add(station1)
    }
    fun findAllPaths(start: String, end: String): List<List<String>> {
        val visited = mutableSetOf<String>()
        val path = mutableListOf<String>()
        val result = mutableListOf<List<String>>()
        dfs(start, end, visited, path, result)
        return result
    }
    fun dfs(
        current: String,
        end: String,
        visited: MutableSet<String>,
        path: MutableList<String>,
        result: MutableList<List<String>>
    ){
        visited.add(current)
        path.add(current)

        if (current == end) {
            result.add(ArrayList(path))
        }
        else {
            graph[current]?.forEach{ neighbor ->
                if (!visited.contains(neighbor)) {
                    dfs(neighbor, end, visited, path, result)
                }
            }
        }

        visited.remove(current)
        path.removeAt(path.size - 1)
    }
    fun calculatePrice(stationCount: Int, userType: String): Int {
        return when (userType) {
            "special" -> 5
            "old" -> when {
                stationCount <= 9 -> 4
                stationCount <= 16 -> 5
                stationCount <= 23 -> 8
                else -> 10
            }
            else -> when {
                stationCount <= 9 -> 8
                stationCount <= 16 -> 10
                stationCount <= 23 -> 15
                else -> 20
            }
        }
    }
    var direction = ""
    fun direction(path:List<String>, lineSearch:List<List<String>>) {

        val directionStart=directionSearch (path[1],path[0],lineSearch)
        direction = ("take ${directionStart[1]} in direction of : ${directionStart[0]} ,")
        var line = searchLine (path[1] ,path[0], lineSearch)


//        val intersectionStations = listOf("sadat", "nasser", "orabi", "al shohadaa", "attaba", "kit kat", "cairo university")
//        for (index in path.indices) {
//            val station = path[index]
//            if (station in intersectionStations && index + 1 < path.size && !lineSearch[line].contains(path[index + 1])) {
//                direction += "intersection at: \"$station\","
//                line = searchLine(station, path[index + 1], lineSearch)
//            }
//        }
        for (index in path.indices) {
            if(path[index]=="sadat" && path[path.size-1]!="sadat" && !lineSearch[line].contains(path[index+1]) )
            {
                print("intersection in :\" sadat \",")
                line = searchLine ("sadat" ,path[index+1], lineSearch)
            }
            if(path[index]=="nasser"  && path[path.size-1]!="nasser" && !lineSearch[line].contains(path[index+1]))
            {
                print("intersection in :\" nasser \",")
                line = searchLine ("nasser" ,path[index+1], lineSearch)
            }
            if(path[index]=="orabi" && path[path.size-1]!="orabi"&& !lineSearch[line].contains(path[index+1]) )
            {
                print("intersection in :\" orabi \",")
                line = searchLine ("orabi" ,path[index+1], lineSearch)
            }
            if(path[index]=="al shohadaa" && path[path.size-1]!="al shohadaa" && !lineSearch[line].contains(path[index+1]) )
            {
                print("intersection in :\" al shohadaa \",")
                line = searchLine ("al shohadaa" ,path[index+1], lineSearch)
            }
            if(path[index]=="attaba"  && path[path.size-1]!="attaba" && !lineSearch[line].contains(path[index+1]))
            {
                print("intersection in :\" attaba \",")
                line = searchLine ("attaba" ,path[index+1], lineSearch)
            }
            if(path[index]=="kit kat" && path[path.size-1]!="kit kat" && !lineSearch[line].contains(path[index+1]) )
            {
                print("intersection in :\"kit kat \",")
                line = searchLine ("kit kat" ,path[index+1], lineSearch)
            }
            if(path[index]=="cairo university" && path[path.size-1]!="cairo university" && !lineSearch[line].contains(path[index+1]) )
            {
                print("intersection in :\"cairo university \",")
                line = searchLine ("cairo university" ,path[index+1], lineSearch)
            }

        }



        val directionEnd = directionSearch(path[path.size - 1], path[path.size - 2], lineSearch)
        direction += (" to ${directionEnd[1]} in direction of : ${directionEnd[0]} \n")
    }
    fun directionSearch (station1:String, station2:String, lineSearch: List<List<String>>): Array<String> {
        var station1Index=0; var station2Last =0 ;
        val directionAndLine = Array(2){""}

        if( (station1 in lineSearch[0] && station2 in lineSearch[0] ))
        {    station1Index = lineSearch[0].indexOf(station1)
            station2Last = lineSearch[0].indexOf(station2)
            directionAndLine[1]= "line one"
            directionAndLine[0] = if( station1Index > station2Last ) "helwan" else "new marg"
        }
        if(station1 in lineSearch[1] && station2 in lineSearch[1] )
        {
            station1Index = lineSearch[1].indexOf(station1)
            station2Last = lineSearch[1].indexOf(station2)
            directionAndLine[1]= "line two"
            directionAndLine[0] = if(station1Index > station2Last) "el monib" else "shubra el khaimah"
        }
        if(station1 in lineSearch[2] && station2 in lineSearch[2] )
        {
            station1Index = lineSearch[2].indexOf(station1)
            station2Last = lineSearch[2].indexOf(station2)
            directionAndLine[1]= "line three"
            directionAndLine[0] = if(station1Index > station2Last) "rod el farag corridor" else "adly mansour"
        }
        if(station1 in lineSearch[3] && station2 in lineSearch[3] )
        {
            station1Index = lineSearch[3].indexOf(station1)
            station2Last = lineSearch[3].indexOf(station2)
            directionAndLine[1]= "line three"
            directionAndLine[0] = if( station1Index > station2Last) "el monib" else "rod el farag corridor"
        }

        return directionAndLine

    }
    fun searchLine (first:String ,second:String, lineSearch: List<List<String>>):Int{

        var lineIndex = 0
        if( (first in lineSearch[0] && second in lineSearch[0] )) lineIndex =0
        if( (first in lineSearch[1] && second in lineSearch[1] )) lineIndex =1
        if( (first in lineSearch[2] && second in lineSearch[2] )) lineIndex =2
        if( (first in lineSearch[3] && second in lineSearch[3] )) lineIndex =3

        return lineIndex
    }
    fun findPath(view: View) {
        val startStation = startStationText.selectedItem.toString()
        val arrivalStation = endStationText.selectedItem.toString()
        if (startStation == arrivalStation){
            Toast.makeText(this, "Error! Start Station = Arrival ,please enter stations again", Toast.LENGTH_LONG).show()
            return
        }
        allPaths = findAllPaths(startStation, arrivalStation)
        val shortestPath = allPaths.minByOrNull { it.size }
        val stationCount = shortestPath?.size ?: 0
        if (allPaths.isEmpty()) {
            Toast.makeText(this, "No valid paths found between $startStation and $arrivalStation.", Toast.LENGTH_SHORT).show()
            return;
        }
        else{
            shortPathText.text="The Shortest Path: ${shortestPath?.joinToString(" -> ")}"
        }
        countStationText.text="Number of Stations: $stationCount"
        timeJourneyText.text="Estimated time: ${(stationCount * 3) / 60} hours ${(stationCount * 3) % 60} minutes"
        val price = calculatePrice(stationCount, "")
        ticketPriceText.text = "Cost of Trip: $price EGP"
        if (shortestPath != null) {
            direction(shortestPath,lines)
            directionText.text = "Direction: $direction"
        }
        showAllPathsButton.isEnabled = true
    }
    fun showAllPaths(view: View) {
        allPathText.text = allPaths.mapIndexed { index, path ->
            "Path ${index + 1}: ${path.joinToString(" -> ")}"
        }.joinToString("\n")
    }
}