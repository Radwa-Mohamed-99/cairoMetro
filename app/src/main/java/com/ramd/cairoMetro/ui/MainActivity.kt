package com.ramd.cairoMetro.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.pojo.LocationCalculations
import com.ramd.cairoMetro.pojo.LocationService
import com.ramd.cairoMetro.pojo.Permissions

class MainActivity : AppCompatActivity() {

    private lateinit var locationTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button



//    private val permissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val allGranted = permissions.entries.all { it.value }
//
//        if (allGranted) {
//            startLocationService()
//        } else {
//            Toast.makeText(this, "Location permissions are required", Toast.LENGTH_LONG).show()
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationTextView = findViewById(R.id.station)
        startButton = findViewById(R.id.getDetail)
        stopButton = findViewById(R.id.shortestPath)

        startButton.setOnClickListener {
//            checkPermissionsAndStartService()
        }




    }

//    override fun onResume() {
//        super.onResume()
//        LocationService.setLocationUpdateListener(this)
//    }
//
//    override fun onPause() {
//        super.onPause()
//        LocationService.setLocationUpdateListener(null)
//    }
//
//    private fun checkPermissionsAndStartService() {
//        if (Permissions(this).hasRequiredPermissions()) {
//            startLocationService()
//        } else {
//            Permissions(this).requestLocationPermissions(permissionLauncher)
//        }
//    }
//
//
//    private fun startLocationService() {
//        val serviceIntent = Intent(this, LocationService::class.java)
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent)
//        } else {
//            startService(serviceIntent)
//        }
//
//        Toast.makeText(this, "Location service started", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun stopLocationService() {
//        val serviceIntent = Intent(this, LocationService::class.java)
//        stopService(serviceIntent)
//        locationTextView.text = "Location updates stopped"
//        Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onLocationChanged(location: Location) {
//        runOnUiThread {
//            var stationData = emptyArray<DataItem>()
//            if (!DataHandling().readUserFromAssets(this, "metro.json").isNullOrEmpty()) {
//                stationData = DataHandling().readUserFromAssets(this, "metro.json") as Array<DataItem>
//            }
//
//        }
//    }


}