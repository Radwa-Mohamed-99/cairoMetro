package com.ramd.cairoMetro.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.ActivityAllRoutesBinding
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.Direction
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.PathsCalculations
import com.ramd.cairoMetro.pojo.Price
import com.ramd.cairoMetro.pojo.StationItem
import com.xwray.groupie.GroupieAdapter
import java.util.ArrayList

class AllRoutes : AppCompatActivity() {
    lateinit var binding: ActivityAllRoutesBinding
    var stationData: Array<DataItem> = emptyArray()
    val dataHandling = DataHandling()
    var paths  = listOf<List<String>>()
    var shortPath = emptyList<String>()
    val items = mutableListOf<StationItem>()
    val price = Price()
    var adapter = GroupieAdapter()
    var index = 0 ; var indexPlus = 0 ;var  indexMins = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAllRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val startStation = intent.getStringExtra("startStation")
        val arrivalStation = intent.getStringExtra("arrivalStation")
        val shortType = intent.getIntExtra("shortType",-1)
        val tripAvailability =  intent.getBooleanExtra("tripAvailability",false)

        if(!dataHandling.readUserFromAssets(this,"metro.json").isNullOrEmpty()) {
            stationData = dataHandling.readUserFromAssets(this,"metro.json") as Array<DataItem>
        }

        val direction = Direction(stationData)
        val pathsCalculations = PathsCalculations(stationData)

        if(!startStation.isNullOrEmpty() && !arrivalStation.isNullOrEmpty()) {
            paths = pathsCalculations.findAllPaths(startStation,arrivalStation)
            if (paths.size > 1) {
                binding.control.isVisible= true
                binding.nextBtn.isEnabled=true
                binding.numberText.text = "${0} / ${paths.size}"
            }
            else {
                binding.control.isVisible= false
            }
        }
        if(shortType  != -1 ) {
            if (shortType == 0) {
                shortPath = pathsCalculations.findShortByStation(paths)

            } else if (shortType == 1) {
                shortPath = direction.shortByIntersections(paths)
            }
            setRecyclerList(shortPath)
            binding.priceText.text = "Price \n${price.calculatePrice(shortPath.size)}"
        }

        if(!tripAvailability)
        {
//            binding.startBtn.isEnabled = false
        }

    }

    @SuppressLint("SetTextI18n")
    fun next(view: View) {
        binding.notesText.isVisible = paths[indexPlus] == shortPath
        index = indexPlus
        setRecyclerList(paths[indexPlus])
        indexMins=indexPlus
        binding.numberText.text= "${indexMins+1} / ${paths.size}"
        indexPlus++
        if(indexPlus>1)
        {
            binding.backBtn.isEnabled=true
        }
        if(indexPlus > (paths.size-1)) {
            binding.nextBtn.isEnabled = false
            return
        }
    }
    fun back(view: View) {
        binding.nextBtn.isEnabled=true ;
        indexPlus=indexMins
        binding.numberText.text= "${indexMins} / ${paths.size}"
        indexMins--
        binding.notesText.isVisible = paths[indexMins] == shortPath
        setRecyclerList(paths[indexMins])
        if(indexMins <= 0){ binding.backBtn.isEnabled=false ;indexPlus=1 ;return}
        index = indexMins
    }
    fun start(view: View) {

        dataHandling.saveSimpleData(this,true,"indicator")
        val b = Intent(this,TripProgress::class.java)
        b.putExtra("trip",paths[index] as ArrayList<String>)
        startActivity(b)

    }

    fun setRecyclerList (path:List<String>){
       items.clear()
        val direction = Direction(stationData)
        val intersections = direction.findIntersections(path)
        for (index in path.indices)
        {
            if (index == 0 )
                items.add( StationItem(path[index], start = true))
            else if ( index == path.size-1)
                items.add( StationItem(path[index], end = true))
            else if(path[index] in  intersections)
                items.add( StationItem(path[index], change = true))
            else
            {
                items.add( StationItem(path[index]))
            }
        }
        adapter = GroupieAdapter()
        adapter.addAll(items)
        binding.StationsLV.adapter= adapter
        val pathCount = path.size
        binding.stationText.text= "Station no. \n${pathCount}"
        binding.timeText.text = "Time \n${(pathCount * 3) / 60} hrs ${(pathCount * 3) % 60} mins"
    }
}