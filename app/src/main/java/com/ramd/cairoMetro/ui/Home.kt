package com.ramd.cairoMetro.ui

import android.R.layout
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.ActivityHomeBinding
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.LocationCalculations
import mumayank.com.airlocationlibrary.AirLocation
import java.util.ArrayList
class Home : AppCompatActivity(),AirLocation.Callback {
    lateinit var binding: ActivityHomeBinding
    val readData = DataHandling()
    var stationData: Array<DataItem> = emptyArray()
    val location = LocationCalculations()
    lateinit var airLocation:AirLocation
    var canStartTrip = false
    val currentLocation = mutableListOf<Double>()
    var path= emptyList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if(!readData.readUserFromAssets(this,"metro.json").isNullOrEmpty()) {
            stationData = readData.readUserFromAssets(this,"metro.json") as Array<DataItem>
        }

        val stationNames = stationData.map { it.name }.toSet().toList()
        val adapter = ArrayAdapter(this, layout.simple_dropdown_item_1line, stationNames)
        binding.start.setAdapter(adapter)
        val adapter2 = ArrayAdapter(this, layout.simple_dropdown_item_1line, stationNames)
        binding.arrival.setAdapter(adapter2)

    ///????????
     airLocation = AirLocation(this, this, true)
     airLocation.start()


        path = readData.getListData(this,"path") .toList()


    }

    fun exchangeStations(view: View) {
        val arrivalStation = binding.arrival.text.toString()
        val startStation =binding.start.text.toString()
     binding.start.setText(arrivalStation,false)
     binding.arrival.setText(startStation,false)

    }

    fun start(view: View) {
        if(!validateStations())return
        val start = binding.start.text.toString()
        val arrival = binding.arrival.text.toString()
        var shortRoute=0
        if (binding.lessTransfer.isChecked )
        {
            shortRoute =1
        }
        val station = location.nearestLocation(stationData,1F,currentLocation[0],currentLocation[1])
        Log.d("path","${currentLocation[0]},${currentLocation[1]}")
        if(station.isNotEmpty())
        {
            canStartTrip = true
        }
        val a = Intent(this,AllRoutes::class.java)
        a.putExtra("startStation",start)
        a.putExtra("arrivalStation",arrival)
        //????
        a.putExtra("shortType",shortRoute)
        a.putExtra("tripAvailability",canStartTrip)
        startActivity(a)
    }

    fun showNearest(view: View) {
        Log.d("path","${currentLocation[0]},${currentLocation[1]}")
        val station = location.nearestLocation(stationData,50F,currentLocation[0],currentLocation[1])
        if(station.isEmpty())
                showToast("no near station from your location")
        else    binding.start.setText(station,false)


    }

    fun map(view: View) {
        val station = binding.start.text.toString()
        if(station.isNotEmpty()) {
            val stationCoordinates = stationData.first { it.name == station}.coordinates
            location.directionFromCurrentMap(stationCoordinates[0].toString(),stationCoordinates[1].toString(),this)
        }
        else
        {
            showToast("enter station in start station")
        }
    }

    fun search(view: View) {
        val address = binding.address.text.toString()
        val startDetails = location.getLatAndLong(this,address)
        if (startDetails == Pair(0.0, 0.0))
            showToast("the address is not valid")
        else if (startDetails == Pair(-1.0, -1.0) )
            showToast(" error while loading the data, try again")
        else {
            val station =location.nearestLocation(stationData,50F,startDetails.first,startDetails.second)

            if(station.isEmpty())
                 showToast( "no near station to your destination")
            else binding.arrival.setText(station,false)
        }
    }

    fun viewAll(view: View) {
        val a = Intent(this , TripProgress::class.java)
        a.putExtra("path",path as ArrayList<String>)
        startActivity(a)
    }

    private fun validateStations(): Boolean {
        return when {
            binding.start.text.isNullOrEmpty() || binding.arrival.text.isNullOrEmpty() -> {
                showToast("Select a station")
                false
            }
            binding.start.text.toString() == binding.arrival.text.toString() -> {
                showToast("Arrival and start station can't be the same")
                false
            }
            else -> true
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun stationDialog () {
        val dataIndicator =  readData.getSimpleData(this,"indicator")
        if(dataIndicator)
        {
            binding.status.isVisible = true
            val station = location.nearestStationPath(stationData,path,currentLocation[0],currentLocation[1])
            if(station.isNotEmpty()) {
                binding.currentStation.text = station
                val stationIndex = path.indexOf(station)
                if (stationIndex < path.size)
                    binding.perviousStation.text = path[stationIndex - 1]
                if (stationIndex > 0)
                    binding.perviousStation.text = path[stationIndex + 1]
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        airLocation.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onSuccess(locations: ArrayList<Location>) {
        currentLocation.add(locations[0].latitude)
        currentLocation.add(locations[0].longitude)
        stationDialog ()
    }

    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
        showToast("$locationFailedEnum")
    }




}