package com.ramd.cairoMetro.pojo


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.ui.TripProgress
import java.util.Locale


class LocationService : Service() {
    private val TAG = "LocationService"
    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "location_service_channel"
    private val UPDATE_INTERVAL = 5000L // 5 seconds - more frequent updates for testing
    private val FASTEST_INTERVAL = 2000L // 2 seconds
   private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null
    var path = emptyList<String>()
    var nearestStation =""
    var previous =  ""
    var stationData = emptyArray<DataItem>()
    val readData = DataHandling()
    var language=""


    // Interface for communication with activities
    interface LocationUpdateListener {
        fun onLocationChanged(location: Location)
    }

    companion object {
        private var locationUpdateListener: LocationUpdateListener? = null

        fun setLocationUpdateListener(listener: LocationUpdateListener?) {
            locationUpdateListener = listener
        }
    }

    override fun onCreate() {

        loadLocale()

        super.onCreate()

        val prefs: SharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        language = prefs.getString("My_Lang", "en") ?: "en"
        Log.d("lan=>","$language")


        if(!readData.readUserFromAssets(this,"metro_$language.json").isNullOrEmpty()) {
            stationData = readData.readUserFromAssets(this,"metro_$language.json") as Array<DataItem>
        }


        path = DataHandling().getListData(this,"path") .toList()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    lastLocation = location
                    Log.d(TAG,"${location.longitude},${location.latitude}")

                    nearestStation = LocationCalculations().nearestStationPath(stationData,1000F, path, location.latitude, location.longitude)
                    if (previous != nearestStation) {
                        notifyUsingDistance(nearestStation)
                        previous = nearestStation
                    }
                    locationUpdateListener?.onLocationChanged(location)
                } ?: run {
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location is not available. User may need to turn on GPS")
                }
            }
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



    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Use highest accuracy
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            smallestDisplacement = 5f // 5 meters minimum displacement
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        getLastLocation()
        startLocationUpdates()
        return START_STICKY
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        lastLocation = location
                        locationUpdateListener?.onLocationChanged(location)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error getting last location: ${e.message}")
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Lost location permission: ${e.message}")
        }
    }

    private fun startLocationUpdates() {
        try {
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

            val client = LocationServices.getSettingsClient(this)
            client.checkLocationSettings(builder.build())
                .addOnSuccessListener {

                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Location settings are not satisfied: ${e.message}")
                    // This would normally show a dialog to fix settings, but we're in a service
                    // Notify user some other way
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Lost location permission: ${e.message}")
        }
    }

    private fun createNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notificon)
            .setColor(Color.RED)
            .setContentTitle(getString(R.string.station_alert))
            .setContentText(getString(R.string.tracking_your_location_during_the_trip))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .also {
                // Create the notification channel for Android O and above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        getString(R.string.location_service_channel),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    notificationManager.createNotificationChannel(channel)
                }
            }
            .build()


    private fun sendPushNotification(alertMessage: String,stage:String) {
        // Select the appropriate image based on the stage
        val imageRes = when (stage) {
            "start" -> R.drawable.start
            "change" -> R.drawable.change
            "end" -> R.drawable.end
            else -> R.drawable.start // Default to start image
        }

        val bigPicture = BitmapFactory.decodeResource(resources, imageRes)
        val resizedImage = Bitmap.createScaledBitmap(bigPicture, 600, 400, true)

        val notification = NotificationCompat.Builder(this, "push_notification_channel")
            .setSmallIcon(R.drawable.ic_stat_notificon)
            .setColor(Color.RED)
            .setContentTitle(getString(R.string.station_alert))
            .setContentText(alertMessage)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(alertMessage))

            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(resizedImage) // Large image
//                    .bigLargeIcon(null) // Hide large icon when expanded
            )

            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, TripProgress::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // This notification can be dismissed
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(2001, notification)
    }
        @SuppressLint("SuspiciousIndentation")
        private fun notifyUsingDistance(station: String) {
        val intersections = Direction(stationData).findIntersections(path)
            if (station == path[0]) {
                sendPushNotification(
                    getString(
                        R.string.starting_trip_soon_in_have_a_nice_trip,
                        station
                    ), "start")
            } else if (station == path[path.size - 1]) {
                sendPushNotification(
                    getString(
                        R.string.reaching_destination_soon_in_have_a_nice_day,
                        station
                    ), "change")
            } else if (station in intersections) {
                sendPushNotification(
                    getString(
                        R.string.reaching_an_intersection_soon_in_be_ready,
                        station
                    ), "end")
            }

        }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )

            val pushChannel = NotificationChannel(
                "push_notification_channel",
                "Station Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you arrive at important stations"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(pushChannel)
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}