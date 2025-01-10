package com.ramd.cairoMetro

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.telecom.Call.Details
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    lateinit var startStation:Spinner
    lateinit var arrivalStation:Spinner
    lateinit var nextPath:Button
    lateinit var perviousPath:Button
    lateinit var shortestPath:Button
    lateinit var price:TextView
    lateinit var time:TextView
    lateinit var stationNo:TextView
    lateinit var direction:TextView
    lateinit var station:TextView
    lateinit var count:TextView
    lateinit var file:SharedPreferences

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
     var indexPlus = 0
    var  indexMins = 0
    var start="";var arrival=""

    val paths = mutableListOf<List<String>>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startStation = findViewById(R.id.startStation)
        arrivalStation = findViewById(R.id.arrivalStation)
        nextPath = findViewById(R.id.nextPath)
        perviousPath = findViewById(R.id.perviousPath)
        shortestPath = findViewById(R.id.shortestPath)
        price = findViewById(R.id.price)
        time = findViewById(R.id.time)
        stationNo = findViewById(R.id.stationNo)
        direction = findViewById(R.id.direction)
        station = findViewById(R.id.station)
        count = findViewById(R.id.count)

        loadData()

    }

    @SuppressLint("SetTextI18n")
    private fun loadData() {
        file = getSharedPreferences("data", MODE_PRIVATE)

        startStation.setSelection(file.getInt("startStation", 0))
        arrivalStation.setSelection(file.getInt("arrivalStation", 0))
        start = file.getString("start" , "").toString()
        arrival= file.getString("arrival" , "").toString()
        indexMins = file.getInt("indexMins",0)
        indexPlus = file.getInt("indexPlus",0)
        station.text = file.getString("path","")
        direction.text = file.getString("direction","")
        stationNo.text = file.getString("stationNumber","")
        price.text = file.getString("price","")
        time.text = file.getString("time","")
        count.text = file.getString("count","")
        nextPath.isEnabled = file.getBoolean("nextButton",false)
        perviousPath.isEnabled = file.getBoolean("previousButton",false)
        shortestPath.isEnabled = file.getBoolean("shortestButton",false)

        if (start != "" && arrival != "") {
            val allPaths = findAllPaths(start, arrival)
            if (allPaths.count()>1)
            {
                val shortPath = allPaths.minByOrNull { it.size }
                for (path in allPaths) {
                    if(path==shortPath)continue
                    paths += listOf(path)

                }

            }
        }
//        Toast.makeText(this, "${paths.size},$indexMins ,$indexPlus", Toast.LENGTH_LONG).show()
    }
    private fun saveData() {
        val indexOfStart = startStation.selectedItemPosition
        val indexOfArrival = arrivalStation.selectedItemPosition
        file.edit {
            putInt("startStation", indexOfStart)
            putInt("arrivalStation", indexOfArrival)
            putInt("indexPlus", indexPlus)
            putInt("indexMins", indexMins)
            putString("start", start)
            putString("arrival", arrival)
            putString("path", station.text.toString())
            putString("direction", direction.text.toString())
            putString("stationNumber", stationNo.text.toString())
            putString("price", price.text.toString())
            putString("time", time.text.toString())
            putString("count", count.text.toString())
            putBoolean("nextButton", nextPath.isEnabled)
            putBoolean("previousButton", perviousPath.isEnabled)
            putBoolean("shortestButton", shortestPath.isEnabled)

        }
    }

    override fun onDestroy() {
        saveData()
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    fun getDetails(view: View)  {
        indexPlus=0
        indexMins = 0
        paths.clear()
        nextPath.isEnabled=false
        perviousPath.isEnabled=false
        shortestPath.isEnabled=false
        count.text = ""

        if(startStation.selectedItemPosition == 0 ||arrivalStation.selectedItemPosition == 0 )
        {
            Toast.makeText(this, "select a station", Toast.LENGTH_SHORT).show()
            return
        }
        if(startStation.selectedItem == arrivalStation.selectedItem) {
            Toast.makeText(this, "arrival and start station can't be the same ", Toast.LENGTH_SHORT).show()
            return
        }

            start = startStation.selectedItem.toString().lowercase()
            arrival = arrivalStation.selectedItem.toString().lowercase()
            val allPaths = findAllPaths(start, arrival)
            val shortPath = allPaths.minByOrNull { it.size }
            station.text= "The Shortest Path:\n  ${shortPath?.joinToString(",")}"
            if (shortPath != null) {
                direction.text = " direction: \n " + direction(shortPath,lines)
            }
            val stationCount = shortPath?.size ?: 0
            stationNo.text= "Station NO \n $stationCount "
            val fees = calculatePrice(stationCount)
            price.text= "Price \n$fees"
            time.text= "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"

         if (allPaths.count()>1)
         {
             nextPath.isEnabled=true
             for (path in allPaths) {
                 if(path==shortPath)continue
                  paths += listOf(path)

             }
             count.text= "${0} / ${paths.size}"
         }

    }

    @SuppressLint("SetTextI18n")
    fun shortest(view: View) {
        val allPaths = findAllPaths(start, arrival)
        val shortPath = allPaths.minByOrNull { it.size }
        station.text= "The Shortest Path:\n  ${shortPath?.joinToString(",")}"
        if (shortPath != null) {
            direction.text = " direction: \n " + direction(shortPath,lines)
        }
        val stationCount = shortPath?.size ?: 0
        stationNo.text= "Station NO \n $stationCount "
        time.text= "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
    }


    @SuppressLint("SetTextI18n")
    fun next (view: View)  {
            station.text = "Another Path:\n  ${paths[indexPlus].joinToString(",")}"
            direction.text = " direction: \n " + direction(paths[indexPlus],lines)
            val stationCount = paths[indexPlus].count()
            stationNo.text= "Station NO \n $stationCount "
            time.text= "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
            indexMins=indexPlus
            count.text= "${indexMins+1} / ${paths.size}"
             indexPlus++
            shortestPath.isEnabled=true
            if(indexPlus>1)
            {
                perviousPath.isEnabled=true
            }
        if(indexPlus > (paths.size-1)) {
            nextPath.isEnabled = false
            return

        }
    }

    @SuppressLint("SetTextI18n")
    fun pervious(view: View) {
            nextPath.isEnabled=true ;
            indexPlus=indexMins
             count.text= "${indexMins} / ${paths.size}"
            indexMins--
            station.text = "Another Path:\n  ${paths[indexMins].joinToString(",")}"
            direction.text = " direction: \n " + direction(paths[indexMins], lines)
            val stationCount = paths[indexMins].count()
            stationNo.text = "Station NO \n $stationCount "
            time.text = "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
           if(indexMins <= 0){ perviousPath.isEnabled=false ;indexPlus=1 ;return}

    }


    init {
        makeMetroGraph()
    }
    private fun addEdge(station1: String, station2: String) {
        graph.putIfAbsent(station1, mutableListOf())
        graph.putIfAbsent(station2, mutableListOf())
        graph[station1]?.add(station2)
        graph[station2]?.add(station1)
    }
    private fun makeMetroGraph() {
        lines.forEach { line ->
            for (i in 0..<line.size-1) {
                addEdge(line[i], line[i + 1])
            }
        }
    }
    private fun findAllPaths(start: String, end: String): List<List<String>> {
        val visited = mutableSetOf<String>()
        val path = mutableListOf<String>()
        val result = mutableListOf<List<String>>()
        dfs(start, end, visited, path, result)
        return result
    }
    private fun dfs(
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

    private fun calculatePrice(stationCount: Int): Int {

       return when {
            stationCount <= 9 -> 8
            stationCount <= 16 -> 10
            stationCount <= 23 -> 15
            else -> 20
        }

    }

    private fun direction(path:List<String>, lineSearch:List<List<String>>):String{

        val directionStart=directionSearch (path[1],path[0],lineSearch)
        var guide = "take ${directionStart[1]} in direction of ${directionStart[0]} ,"
        var line = searchLine (path[1] ,path[0], lineSearch)
        var found = false



        val intersectionStations = listOf("sadat", "nasser", "orabi",  "ataba", "al shohadaa","kit kat", "cairo university")
        for (index in path.indices) {
            val station = path[index]
            if (station in intersectionStations && (index + 1 )< path.size && !lineSearch[line].contains(path[index + 1])) {
                guide += "intersection at: \"$station\","
                line = searchLine(station, path[index + 1], lineSearch)
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

    private fun searchLine (first:String ,second:String, lineSearch: List<List<String>>):Int{
        for (i in lineSearch.indices) {
            if (first in lineSearch[i] && second in lineSearch[i]) {
                return i
            }
        }
        return -1
    }




}


