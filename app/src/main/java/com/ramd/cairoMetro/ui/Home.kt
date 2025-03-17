package com.ramd.cairoMetro.ui

import android.R.layout
import android.content.Intent
import android.content.SharedPreferences
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
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.LocationCalculations
import com.ramd.cairoMetro.pojo.LocationService
import mumayank.com.airlocationlibrary.AirLocation
import java.util.Locale
import android.content.res.Configuration
import androidx.appcompat.app.AlertDialog

class Home : AppCompatActivity(),AirLocation.Callback {
    lateinit var binding: ActivityHomeBinding
    val readData = DataHandling()
    var stationData: Array<DataItem> = emptyArray()
    val location = LocationCalculations()
    lateinit var airLocation:AirLocation
    var currentLocation = mutableListOf<Double>()
    var path= emptyList<String>()
    var station =""
    var language=""

    override fun onCreate(savedInstanceState: Bundle?) {
        loadLocale()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


//        this.setContentView(R.layout.activity_home)

        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
         language = prefs.getString("My_Lang", "en") ?: "en"
        Log.d("lan=>","$language")


        if(!readData.readUserFromAssets(this,"metro_$language.json").isNullOrEmpty()) {
            stationData = readData.readUserFromAssets(this,"metro_$language.json") as Array<DataItem>
        }

        val stationNames = stationData.map { it.name }.toSet().toList()
        val adapter = ArrayAdapter(this, layout.simple_dropdown_item_1line, stationNames)
        binding.start.setAdapter(adapter)
        val adapter2 = ArrayAdapter(this, layout.simple_dropdown_item_1line, stationNames)
        binding.arrival.setAdapter(adapter2)

        airLocation = AirLocation(this, this, false,10000)
        airLocation.start()

        path = DataHandling().getListData(this,"path").toList()


    }


    fun changeLanguage(view: View) {
        showLanguageDialog()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("\uD83C\uDDFA\uD83C\uDDF8 English", "\uD83C\uDDE6\uD83C\uDDEA العربية")
        val languageCodes = arrayOf("en", "ar")

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Language")

        builder.setItems(languages) { _, which ->
            val selectedLanguage = languageCodes[which]
            switchLanguage(selectedLanguage)
        }

        builder.show()
    }

    private fun switchLanguage(lang: String) {
        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = prefs.edit()
        editor.putString("My_Lang", lang)
        editor.apply()

        setLocale(lang)
    }
    private fun setLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        val intent = Intent(this, Home::class.java)
        startActivity(intent)
        finish()
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




    override fun onBackPressed() {
        stationData = emptyArray()
        path =emptyList<String>()
        currentLocation.clear()
        airLocation = AirLocation(this, this, true)
        airLocation.start()
        finishAffinity()
        super.onBackPressed()
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

        val shortRoute =binding.lessTransfer.isChecked

        val station = location.nearestLocation(stationData,1F,currentLocation[0],currentLocation[1])

        val a = Intent(this,AllRoutes::class.java)
        a.putExtra("startStation",start)
        a.putExtra("arrivalStation",arrival)
        a.putExtra("shortType",shortRoute)
        a.putExtra("tripAvailability",station.isNotEmpty())
        startActivity(a)
    }

    fun showNearest(view: View) {
        airLocation.start()
        val station = location.nearestLocation(stationData,100F,currentLocation[0],currentLocation[1])
        if(station.isEmpty())
            showToast(getString(R.string.no_near_station_from_your_location))
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
            showToast(getString(R.string.enter_station_in_start_station))
        }
    }

    fun search(view: View) {
        val address = binding.address.text.toString()
        val startDetails = location.getLatAndLong(this,address)
        if (startDetails == Pair(0.0, 0.0))
            showToast(getString(R.string.the_address_is_not_valid))
        else if (startDetails == Pair(-1.0, -1.0) )
            showToast(getString(R.string.error_while_loading_the_data_try_again))
        else {
            val station =location.nearestLocation(stationData,50F,startDetails.first,startDetails.second)

            if(station.isEmpty())
                showToast(getString(R.string.no_near_station_to_your_destination))
            else binding.arrival.setText(station,false)
        }
    }

    fun viewAll(view: View) {
        val a = Intent(this , TripProgress::class.java)
        a.putExtra("station",station)
        a.putExtra("path",path as ArrayList<String>)
        startActivity(a)
    }

    private fun validateStations(): Boolean {
        return when {
            binding.start.text.isNullOrEmpty() || binding.arrival.text.isNullOrEmpty() -> {
                showToast(getString(R.string.select_a_station))
                false
            }
            binding.start.text.toString() == binding.arrival.text.toString() -> {
                showToast(getString(R.string.arrival_and_start_station_can_t_be_the_same))
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
        Log.d("coordinates", "$dataIndicator")
        Log.d("coordinates", "${path.size}")
        if(dataIndicator && path.isNotEmpty())
        {
            station = location.nearestStationPath(stationData,1000F,path,currentLocation[0],currentLocation[1])
            Log.d("coordinates", "$station")

            if (station == path.last() )
            {
                readData.saveSimpleData(this,false,"indicator")
                val serviceIntent = Intent(this, LocationService::class.java)
                stopService(serviceIntent)
                Toast.makeText(this,
                    getString(R.string.location_service_stopped), Toast.LENGTH_SHORT).show()

            }
            if ( station.isNotEmpty()) {
                binding.status.isVisible = true

                binding.currentStation.text = station
                val stationIndex = path.indexOf(station)
                if (stationIndex < path.size-1) {
                    binding.nextStation.text = path[stationIndex + 1]
                    Log.d("coordinates", "${path[stationIndex + 1]}")

                }
                else{binding.nextStation.text = ""}
                if (stationIndex > 0) {
                    binding.perviousStation.text = path[stationIndex - 1]
                }
                else{binding.perviousStation.text = ""}


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
        currentLocation.clear()
        currentLocation.add(locations[0].latitude)
        currentLocation.add(locations[0].longitude)
        stationDialog ()
        Log.d("coordinates","${currentLocation[0]} + ${currentLocation[1]} ")

    }

    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
//        showToast("$locationFailedEnum")
    }




}