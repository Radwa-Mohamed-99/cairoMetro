package com.ramd.cairoMetro.ui

import android.Manifest
import com.google.android.gms.location.*
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.pojo.StationItem
import com.ramd.cairoMetro.databinding.ActivityTripProgressBinding
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.Direction
import com.ramd.cairoMetro.pojo.LocationCalculations
import com.ramd.cairoMetro.pojo.MyWork
import com.ramd.cairoMetro.pojo.Price
import com.xwray.groupie.GroupieAdapter
import mumayank.com.airlocationlibrary.AirLocation
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import androidx.work.Data
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class TripProgress : AppCompatActivity(), AirLocation.Callback {

    lateinit var binding: ActivityTripProgressBinding
    var items = mutableListOf<StationItem>()
    var adapter = GroupieAdapter()
    var path: List<String> = emptyList()
    val location = LocationCalculations()
    lateinit var airLocation: AirLocation
    var stationData = emptyArray<DataItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTripProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("TripProgress=>", "onCreate called")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        if (!DataHandling().readUserFromAssets(this, "metro.json").isNullOrEmpty()) {
            stationData = DataHandling().readUserFromAssets(this, "metro.json") as Array<DataItem>
        }

        var pathCheck = intent.getStringArrayListExtra("trip")
        Log.d("TripProgress", "Received path: $pathCheck")

        if (!pathCheck.isNullOrEmpty()) {
            path = pathCheck
            setRecyclerList(path)
            Log.e("path=>", "$path")
        } else {
            pathCheck = intent.getStringArrayListExtra("path")
            Log.e("PathCheckElse=>", "$pathCheck")
            if (!pathCheck.isNullOrEmpty()) {
                path = pathCheck
                setRecyclerList(path)
            }
        }

        airLocation = AirLocation(this, this, true)

        val timer = (path.size * 3 * 60 * 1000).toLong()
        object : CountDownTimer(timer, 3000) {
            override fun onTick(millisUntilFinished: Long) {
                airLocation.start()
            }

            override fun onFinish() {

            }
        }.start()

    }


    override fun onBackPressed() {
        DataHandling().saveListData(this, path.toTypedArray(), "path")
        val a = Intent(this, Home::class.java)
        startActivity(a)
        finish()
        super.onBackPressed()
    }

    //
    fun cancel(view: View) {
        DataHandling().saveSimpleData(this, false, "indicator")
        val a = Intent(this, Home::class.java)
        startActivity(a)
    }


    fun setRecyclerList(path: List<String>) {
        setDataInRecycler("")
        val pathCount = path.size
        val price = Price()
        binding.stationNumbers.text = "Station no. \n${pathCount}"
        binding.timeTrip.text = "Time \n${(pathCount * 3) / 60} hrs ${(pathCount * 3) % 60} mins"
        binding.priceStation.text = "Price \n${price.calculatePrice(pathCount)}"
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        airLocation.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        airLocation.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    //
    override fun onSuccess(locations: ArrayList<Location>) {
//كود رضوي
//        val station = location.nearestStationPath(
//            stationData,
//            path,
//            locations[0].latitude,
//            locations[0].longitude
//        )
//        Log.d("location", "${locations[0].latitude},${locations[0].longitude}")
//        setDataInRecycler(station)

        if (locations.isEmpty()) {
            Log.e("Location", "No location data available")
            return
        }
//
        val userLat = locations[0].latitude
        val userLong = locations[0].longitude

        val nearestStation = location.nearestStationPath(stationData, path, userLat, userLong)
        Log.d("location", "User location: $userLat, $userLong")
        notifyUsingDistance(nearestStation)
        setDataInRecycler(nearestStation)
        Log.e("nearestStation=>","$nearestStation")
    }

    private fun notifyUsingDistance(station: String) {
        val intersections = Direction(stationData).findIntersections(path)

        if (station == path[0]) {
            showNotification(this, station, "Start")
        } else if (station == path[path.size - 1]) {
            showNotification(this, station, "End")
        } else if (station in intersections) {
                showNotification(this, station, "Change")
        }

    }

    override fun onFailure(locationFailedEnum: AirLocation.LocationFailedEnum) {
        showToast("$locationFailedEnum")
    }

    //
    private fun setDataInRecycler(station: String) {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        items.clear()
        val intersections = Direction(stationData).findIntersections(path)
        var currentFlag = false
        for (index in path.indices) {
            if (station == path[index]) currentFlag = true
            if (index == 0) {
                items.add(StationItem(path[index], start = true, stationState = currentFlag))
            } else if (index == path.size - 1) {
                items.add(StationItem(path[index], end = true, stationState = currentFlag))
            } else {
                if (path[index] in intersections) {
                    items.add(StationItem(path[index], change = true, stationState = currentFlag))
                } else {
                    items.add(StationItem(path[index], stationState = currentFlag))
                }
            }
        }
        adapter.clear()
        adapter.update(items)
        val state = items.indexOfFirst { it.stationState }
        binding.recyclerView.adapter = adapter
        layoutManager.scrollToPosition(state)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun showNotification(context: Context, stationName: String, stationType: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
            return
        }


        val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val data = Data.Builder()
                .putString("STATION_NAME", stationName)
                .putString("STATION_TYPE", stationType)
                .build()

            val request = OneTimeWorkRequest.Builder(MyWork::class.java)
                .setConstraints(constraints)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueue(request)

    }


}
