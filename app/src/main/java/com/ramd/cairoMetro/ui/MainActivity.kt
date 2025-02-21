package com.ramd.cairoMetro.ui
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ramd.cairoMetro.pojo.DataItem
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.ActivityMainBinding
import com.ramd.cairoMetro.pojo.Direction
import com.ramd.cairoMetro.pojo.DataHandling
import com.ramd.cairoMetro.pojo.MyWork
import com.ramd.cairoMetro.pojo.PathsCalculations
import com.ramd.cairoMetro.pojo.Price


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
     var indexPlus = 0
     var  indexMins = 0
     var start="";var arrival=""
     val paths = mutableListOf<List<String>>()
     var data: Array<DataItem> = emptyArray()
    val calculatePrice=Price()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val readData = DataHandling()
        if (!readData.readUserFromAssets(this, "metro.json").isNullOrEmpty()) {
            data = readData.readUserFromAssets(this, "metro.json") as Array<DataItem>
        }


        val suggestions = data.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, suggestions)
        binding.startStation.setAdapter(adapter)


        }


        fun getDetails(view: View) {
            indexPlus = 0
            indexMins = 0
            paths.clear()
            binding.nextPath.isEnabled = false
            binding.perviousPath.isEnabled = false
            binding.shortestPath.isEnabled = false
            binding.count.text = ""


            if (binding.startStation.text.isNullOrEmpty() || binding.arrivalStation.selectedItemPosition == 0) {
                Toast.makeText(this, "select a station", Toast.LENGTH_SHORT).show()
                return
            } else if (binding.startStation.text.toString() == binding.arrivalStation.selectedItem) {
                Toast.makeText(
                    this,
                    "arrival and start station can't be the same ",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            start = binding.startStation.text.toString().lowercase()
            arrival = binding.arrivalStation.selectedItem.toString().lowercase()
            val pathsCalculations = PathsCalculations(data)
            Log.d("status", "${start},$arrival")
            val allPaths = pathsCalculations.findAllPaths(start, arrival)
            Log.d("status", "${allPaths.size}")
//                val shortPath = direction.shortByIntersections(allPaths)
            val shortPath = allPaths.minBy { it.size }

            binding.station.text = "The Shortest Path:\n  ${shortPath.joinToString(",")}"
            binding.direction.text = " direction: \n " + fullDirectionPath(shortPath)
            val stationCount = shortPath.size ?: 0
            binding.stationNo.text = "Station NO \n $stationCount "
            val fees = calculatePrice.calculatePrice(stationCount)
            binding.price.text = "Price \n$fees"
            binding.time.text =
                "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"

            if (allPaths.count() > 1) {
                binding.nextPath.isEnabled = true
                for (path in allPaths) {
                    if (path == shortPath) continue
                    paths += listOf(path)

                }
                binding.count.text = "${0} / ${paths.size}"
            }


        }

        fun shortest(view: View) {
            val pathsCalculations = PathsCalculations(data)
            val allPaths = pathsCalculations.findAllPaths(start, arrival)
            val shortPath = allPaths.minByOrNull { it.size }
            binding.station.text = "The Shortest Path:\n  ${shortPath?.joinToString(",")}"
            if (shortPath != null) {
                binding.direction.text = " direction: \n " + fullDirectionPath(shortPath)
            }
            val stationCount = shortPath?.size ?: 0
            binding.stationNo.text = "Station NO \n $stationCount "
            binding.time.text =
                "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
        }


        @SuppressLint("SetTextI18n")
        fun next(view: View) {
            binding.station.text = "Another Path:\n  ${paths[indexPlus].joinToString(",")}"
            binding.direction.text = " direction: \n " + fullDirectionPath(paths[indexPlus])
            val stationCount = paths[indexPlus].count()
            binding.stationNo.text = "Station NO \n $stationCount "
            binding.time.text =
                "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
            indexMins = indexPlus
            binding.count.text = "${indexMins + 1} / ${paths.size}"
            indexPlus++
            binding.shortestPath.isEnabled = true
            if (indexPlus > 1) {
                binding.perviousPath.isEnabled = true
            }
            if (indexPlus > (paths.size - 1)) {
                binding.nextPath.isEnabled = false
                return

            }
        }

        @SuppressLint("SetTextI18n", "SuspiciousIndentation")
        fun pervious(view: View) {
            binding.nextPath.isEnabled = true;
            indexPlus = indexMins
            binding.count.text = "${indexMins} / ${paths.size}"
            indexMins--
            binding.station.text = "Another Path:\n  ${paths[indexMins].joinToString(",")}"
            binding.direction.text = " direction: \n " + fullDirectionPath(paths[indexMins])
            val stationCount = paths[indexMins].count()
            binding.stationNo.text = "Station NO \n $stationCount "
            binding.time.text =
                "time \n ${(stationCount * 3) / 60} hrs ${(stationCount * 3) % 60} mins"
            if (indexMins <= 0) {
                binding.perviousPath.isEnabled = false;indexPlus = 1;return
            }

        }

        fun findDirection(first: String, second: String): List<String> {
            var lineName = ""
            var directionOfStation = ""
            val list = mutableListOf("", "")
            val lineNames = listOf("line one", "line two", "line three", "line three1")
            val directions = listOf(
                Pair("helwan", "new marg"),
                Pair("el monib", "shubra el khaimah"),
                Pair("rod el farag corridor", "adly mansour"),
                Pair("el monib", "rod el farag corridor")
            )
            val direction = Direction(data)

            lineName = direction.findLine(first, second)
            val firstItem = data.filter { it.name == first }.first { it.line == lineName }
            val secondItem = data.filter { it.name == second }.first { it.line == lineName }
            val index = lineNames.indexOf(lineName)
            if (firstItem.id < secondItem.id) {
                directionOfStation = directions[index].first
            } else {
                directionOfStation = directions[index].second
            }
            list[1] = directionOfStation
            list[0] = lineName
            return list
        }

        fun fullDirectionPath(path: List<String>): String {
            var fullDirection = ""
            var line = findDirection(path[0], path[1])
            fullDirection += "take ${line[0]} in direction of ${line[1]} ,"
            val direction = Direction(data)

            val intersections = direction.findIntersections(path)

            if (intersections.isEmpty()) {
                fullDirection += "and there are no intersections "
            } else {
                for (intersection in intersections) {
                    fullDirection += "intersection at: \"$intersection\","
                }
                line = findDirection(path[path.size - 2], path[path.size - 1])
                fullDirection += "to ${line[0]} in direction of ${line[1]} \n"
            }

            return fullDirection

        }




//    object : CountDownTimer(18000, 1000) {
//
//        override fun onTick(millisUntilFinished: Long) {
//            val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
//            val scrollPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
//            adapter = GroupieAdapter()
//            items.clear()
//            x++
//            for (index in stations.indices)
//            {
//                var flag = false
//                if(stations[index] == stations[x])
//                {
//                    flag = true
//                    passed.add(stations[x])
//                }
//                var flag2 = false
//                if (stations[x-1] == passed[(passed.size)-1])
//                    flag2 = true
//                if (index == 0 )
//                    items.add( StationItem(stations[index], stationState = flag, start = true, passed = flag2))
//                else if ( index == stations.size-1)
//                    items.add( StationItem(stations[index], stationState = flag, end = true, passed = flag2))
//                else if(stations[index] == "ataba")
//                    items.add( StationItem(stations[index], stationState = flag, change = true, passed = flag2))
//                else
//                {
//                    items.add( StationItem(stations[index],flag, passed = flag2))
//                }
//            }
//            adapter.clear()
//            adapter.update(items)
//            val state  =  items.indexOfFirst { it.stationState }
//            binding.recyclerView.adapter= adapter
//            layoutManager.scrollToPosition(state)
//        }
//
//        override fun onFinish() {
//
//        }
//    }.start()


//    private fun showNotification( stationName: String, stationType: String) {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                1
//            )
//        } else {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//
//            val data = Data.Builder()
//                .putString("STATION_NAME", stationName)
//                .putString("STATION_TYPE", stationType)
//                .build()
//
//            val request = OneTimeWorkRequest.Builder(MyWork::class.java)
//                .setConstraints(constraints)
//                .setInputData(
//                    data
//                )
//                .setInitialDelay(
//                    15,
//                    TimeUnit.SECONDS
//                )
//                .build()
//
//            WorkManager.getInstance(this).enqueue(request)
//        }
//    }


//object : CountDownTimer(timer, 3000) {
//    override fun onTick(millisUntilFinished: Long) {
//        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
//        items.clear()
//
//        var locations: ArrayList<Location> = arrayListOf(Location(""))
//
//        val station = location.nearestStationPath(
//            stationData,
//            path,
//            locations[0].latitude,
//            locations[0].longitude
//        )
//        val intersections = Direction(stationData).findIntersections(path)
//        var currentFlag = false
//        for (index in path.indices) {
//            if (station == path[index]) currentFlag = true
//            if (index == 0) {
//                items.add(StationItem(path[index], start = true, stationState = currentFlag))
//                showNotification(station, "Start")
//            } else if (index == path.size - 1) {
//                items.add(StationItem(path[index], end = true, stationState = currentFlag))
//                showNotification(station, "End")
//            } else {
//                if (path[index] in intersections) {
//                    items.add(StationItem(path[index], change = true, stationState = currentFlag))
//                    showNotification(station, "Change")
//                } else {
//                    items.add(StationItem(path[index], stationState = currentFlag))
//                }
//            }
//        }
//        adapter.clear()
//        adapter.update(items)
//        val state = items.indexOfFirst { it.stationState }
//        binding.recyclerView.adapter = adapter
//        layoutManager.scrollToPosition(state)
//    }
//
//    override fun onFinish() {
//
//    }
//}.start()






    //    private fun notifyUsingDistance(locations: ArrayList<Location>) {
//        for (stationName in path) {
//            Log.e("i=>", "$i")
//            i++
//            val stationCoordinates = stationData.firstOrNull { it.name == stationName }?.coordinates
//            Log.e("stCs=>", "$stationCoordinates")
//
//            if (stationCoordinates == null) continue
//
//            val stationLocation = Location("").apply {
//                latitude = stationCoordinates[0]
//                longitude = stationCoordinates[1]
//            }
//
//            val distance = locations[0].distanceTo(stationLocation) / 1000
//
//            val d = 450F
//            Log.e("Location", "Distance to $stationName: $distance km val d= $d ")
//
//
//            val stationItem = items.find { it.station == stationName }
//            if (stationItem != null) {
//                Log.e("stationItem=>",
//                    "start:${stationItem.start} end:${stationItem.end} $stationName"
//                )
//                if (stationItem.start) {
//                    Log.e("send=>", "start")
//                    showNotification(this, stationName, "Start")
//                } else {
//                    if (distance <= d) {
//                        when {
//                            stationItem.end -> {
//                                Log.e("send=>", "end")
//                                showNotification(this, stationName, "End")
//                            }
//
//                            stationItem.change -> {
//                                Log.e("send=>", "change")
//                                showNotification(this, stationName, "change")
//                            }
//                            else -> {
//                                Log.e("send=>", "normal")
//                            }
//                        }
//                    }
//                }
//            }
////            break
//        }
//
//    }


//        val notif = NotificationCompat.Builder(applicationContext, "default")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle("You Now at $stationType Station")
//            .setContentText("$stationName")
//            .setStyle(NotificationCompat.BigTextStyle()
//            .bigText("You have arrived at $stationName.\nHave a great trip!"))
//            .setColor(Color.parseColor("#FFA500"))
//            .setVibrate(longArrayOf(0, 500, 250, 500))
//            .setLights(Color.BLUE, 500, 2000)

//            .setAutoCancel(true)
//            .setContentIntent(pe)
//            .build()

//        try {
//            NotificationManagerCompat.from(applicationContext).notify(1, notif)
//        } catch (e: SecurityException) {
//            Log.e("NotificationError", "Failed to show notification: ${e.message}")
//        }


//    private fun showNotification(context: Context, stationName: String, stationType: String) {
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                context as Activity,
//                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                1
//            )
//            return
//        }
//
//
////        Log.e("showmessageFun","yes")
////        val channelId = "station_alerts"
////        val notificationManager =
////            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
////            val channel = NotificationChannel(
////                channelId,
////                "Station Alerts",
////                NotificationManager.IMPORTANCE_HIGH
////            )
////            channel.description = "Notifications for nearby metro stations"
////            notificationManager.createNotificationChannel(channel)
////        }
//
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
//
//        val data = Data.Builder()
//            .putString("STATION_NAME", stationName)
//            .putString("STATION_TYPE", stationType)
//            .build()
//
//        val request = OneTimeWorkRequest.Builder(MyWork::class.java)
//            .setConstraints(constraints)
//            .setInputData(data)
////                .setInitialDelay(15,TimeUnit.SECONDS)
//            .build()
//
//        WorkManager.getInstance(this).enqueue(request)
//
////        val a = Intent(this, TripProgress::class.java)
////        a.apply {
////            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
////        }
////
////        val requestCode = System.currentTimeMillis().toInt()
////
////        val pe = PendingIntent.getActivity(
////            this, requestCode, a, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
////        )
////
////        val smallIcon = R.drawable.ic_launcher_foreground
////        val bigPicture = BitmapFactory.decodeResource(context.resources, R.drawable.station_image)
////        if (bigPicture != null) {
////            val resizedImage = Bitmap.createScaledBitmap(bigPicture, 600, 400, true)
////            val builder = NotificationCompat.Builder(context, channelId)
////                .setSmallIcon(smallIcon)
////                .setContentTitle("\uD83D\uDE89 Station Alert :")
////                .setContentText(
////                    when {
////                        stationType.equals(
////                            "Start",
////                            ignoreCase = true
////                        ) -> "You Now at ($stationName) Have a nice trip!"
////
////                        stationType.equals(
////                            "change",
////                            ignoreCase = true
////                        ) -> "You are 10 meters away from: $stationName ($stationType station)"
////
////                        else -> "You soon will arrive at ($stationName) Hope you had a great journey!"
////                    }
////                )
////
////                .setStyle(
////                    NotificationCompat.BigPictureStyle()
////                        .bigLargeIcon(resizedImage)
////                        .bigPicture(resizedImage)
////                )
////                .setPriority(NotificationCompat.PRIORITY_HIGH)
////                .setAutoCancel(true)
////                .setContentIntent(pe)
////                .setWhen(System.currentTimeMillis())
//////            val notificationId = System.currentTimeMillis().toInt()
//////
//////            val notificationId = when {
//////                stationType.equals("Start",ignoreCase = true) -> 1
//////                stationType.equals("change",ignoreCase = true) -> 2
//////                else -> System.currentTimeMillis().toInt()
//////            }
//////
//////            notificationManager.notify(notificationId, builder.build())
////
////
////            notificationManager.notify(1, builder.build())
////
////        } else {
////            Log.e("Notification", "Failed to load station_image")
////        }
//    }
//

}


