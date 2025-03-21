package com.ramd.cairoMetro.ui


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.pojo.StationItem
import com.ramd.cairoMetro.databinding.ActivityTripProgressBinding
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.Direction
import com.ramd.cairoMetro.pojo.LocationCalculations
import com.ramd.cairoMetro.pojo.LocationService
import com.ramd.cairoMetro.pojo.Permissions
import com.ramd.cairoMetro.pojo.Price
import com.xwray.groupie.GroupieAdapter
import mumayank.com.airlocationlibrary.AirLocation
import java.util.Locale


class TripProgress : AppCompatActivity(), LocationService.LocationUpdateListener{

    lateinit var binding: ActivityTripProgressBinding
    val readData = DataHandling()
    var items = mutableListOf<StationItem>()
    var adapter = GroupieAdapter()
    var path: List<String> = emptyList()
    var stationData = emptyArray<DataItem>()
    var previousStation =""
    var language=""

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }

        if (allGranted) {
            startLocationService()
        } else {
            Toast.makeText(this,
                getString(R.string.location_permissions_are_required), Toast.LENGTH_LONG).show()
        }
    }



    @SuppressLint("SetTextI18n", "StringFormatMatches")
    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTripProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        language = prefs.getString("My_Lang", "en") ?: "en"
        Log.d("lan=>","$language")


        if(!readData.readUserFromAssets(this,"metro_$language.json").isNullOrEmpty()) {
            stationData = readData.readUserFromAssets(this,"metro_$language.json") as Array<DataItem>
        }

        previousStation = intent.getStringExtra("station").toString()

        var pathCheck = intent.getStringArrayListExtra("trip")
        if (!pathCheck.isNullOrEmpty()) {
            path = pathCheck
            setDataInRecycler(previousStation)
        } else {
            pathCheck = intent.getStringArrayListExtra("path")
            if (!pathCheck.isNullOrEmpty()) {
                path = pathCheck
                setDataInRecycler(previousStation)
            }
            else
            {
                if(DataHandling().getListData(this,"path").isNotEmpty()){
                path = DataHandling().getListData(this,"path").toList()}
                Log.d("coordinates", "${path.size}")
            }
        }

        val pathCount = path.size
        val price = Price()
//        binding.stationNumbers.text = "Station no. \n${pathCount}"
//        binding.timeTrip.text = "Time \n${(pathCount * 3) / 60} hrs ${(pathCount * 3) % 60} mins"
//        binding.priceStation.text = "Price \n${price.calculatePrice(pathCount)}"

        binding.stationNumbers.text = getString(R.string.station_no, pathCount)
        binding.timeTrip.text = getString(R.string.time_hrs_mins, (pathCount * 3) / 60, (pathCount * 3) % 60)
        binding.priceStation.text =
            getString(R.string.price, price.calculatePrice(pathCount))

        if (!DataHandling().getSimpleData(this,"indicator")) {
            checkPermissionsAndStartService()
            DataHandling().saveSimpleData(this, true, "indicator")
        }

    }
    private fun loadLocale() {

        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val language = prefs.getString("My_Lang", "en") ?: "en"

        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        resources.updateConfiguration(config, resources.displayMetrics)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    override fun onDestroy() {
        items.clear()
        adapter.clear()
        stationData = emptyArray()
        super.onDestroy()
    }


    override fun onBackPressed() {

        DataHandling().saveListData(this, path.toTypedArray(), "path")
        val a = Intent(this, Home::class.java)
        startActivity(a)
        super.onBackPressed()
    }


    fun cancel(view: View) {
        stopLocationService()
        DataHandling().saveSimpleData(this, false, "indicator")
        val a = Intent(this, Home::class.java)
        startActivity(a)
    }

    private fun setDataInRecycler(station: String) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        items.clear()
        val intersections = Direction(stationData).findIntersections(path)

        for (index in path.indices) {
            if (index == 0 ) {
                if(station == path[index])
                    items.add(StationItem(path[index], start = true, stationState =true, context = this.applicationContext ))
                else items.add(StationItem(path[index], start = true, context = this.applicationContext))
            } else if (index == path.size - 1) {
                if(station == path[index])
                    items.add(StationItem(path[index], end = true, stationState =true , context = this.applicationContext))
                else items.add(StationItem(path[index], end = true, context = this.applicationContext))
            } else if (path[index] in intersections) {
                if(station == path[index])
                    items.add(StationItem(path[index], change = true, stationState =true , context = this.applicationContext))
                else items.add(StationItem(path[index], change = true, context = this.applicationContext))
            } else {
                if(station == path[index])
                    items.add(StationItem(path[index], stationState =true , context = this.applicationContext))
                else items.add(StationItem(path[index], context = this.applicationContext))
            }
        }
        adapter.clear()
        adapter.update(items)
        val state = items.indexOfFirst { it.stationState }
        binding.recyclerView.adapter = adapter
        layoutManager.scrollToPosition(state)
    }


    override fun onResume() {
        super.onResume()
        LocationService.setLocationUpdateListener(this)
    }

    override fun onPause() {
        super.onPause()
        LocationService.setLocationUpdateListener(null)
    }

    private fun checkPermissionsAndStartService() {
        if (Permissions(this).hasRequiredPermissions()) {
            startLocationService()
        } else {
            Permissions(this).requestLocationPermissions(permissionLauncher)
        }
    }


    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, getString(R.string.location_service_stopped), Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(location: Location) {
        runOnUiThread {
            var stationData = emptyArray<DataItem>()

            val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
            language = prefs.getString("My_Lang", "en") ?: "en"
            Log.d("lan=>","$language")

            if(!readData.readUserFromAssets(this,"metro_$language.json").isNullOrEmpty()) {
                stationData = readData.readUserFromAssets(this,"metro_$language.json") as Array<DataItem>
            }

            val nearestStation = LocationCalculations().nearestStationPath(stationData,1000F,path, location.latitude, location.longitude)
            if (nearestStation != previousStation) {
                setDataInRecycler(nearestStation)
               previousStation= nearestStation
            }
            if (nearestStation == path[path.size-1] )
            {
                DataHandling().saveSimpleData(this,false,"indicator")
                stopLocationService()
            }

        }
    }




}
