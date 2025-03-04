package com.ramd.cairoMetro.pojo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import android.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.ui.TripProgress

class MyWork(context: Context, params: WorkerParameters) :
    Worker(context, params) {
    override fun doWork(): Result {
        Log.d("workManager", "completed")
        createChannel()
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val stationName = inputData.getString("STATION_NAME")
        val stationType = inputData.getString("STATION_TYPE")
        val a = Intent(applicationContext, TripProgress::class.java)
        a.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val requestCode = System.currentTimeMillis().toInt()
        val pe = PendingIntent.getActivity(
            applicationContext,
            requestCode,
            a,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val smallIcon = R.drawable.ic_stat_notificon
        val bigPicture = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.station_image)
        val pic=BitmapFactory.decodeResource(applicationContext.resources,R.drawable.ic_stat_notificon)


        if (bigPicture != null) {
            val resizedImage = Bitmap.createScaledBitmap(bigPicture, 600, 400, true)
            val resizedImage2 = Bitmap.createScaledBitmap(bigPicture, 600, 400, true)
            val channelId = "station_alerts"
            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(smallIcon)
                .setColor(Color.RED)
                .setContentTitle("\uD83D\uDE89 Station Alert :")
                .setContentText(
                    when {
                        stationType.equals(
                            "Start",
                            ignoreCase = true
                        ) -> "You Now at ($stationName) Have a nice trip!"

                        stationType.equals(
                            "change",
                            ignoreCase = true
                        ) -> "You are 10 meters away from: $stationName ($stationType station)"

                        else -> "You soon will arrive at ($stationName) Hope you had a great journey!"
                    }
                )

                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigLargeIcon(pic)
                        .bigPicture(resizedImage)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pe)
                .setWhen(System.currentTimeMillis())
            notificationManager.notify(1, builder.build())

        } else {
            Log.e("Notification", "Failed to load station_image")
        }
        return Result.success()
    }


    fun createChannel() {
        val channelId = "station_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Station Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for nearby metro stations"
            val notifManager = applicationContext.getSystemService(NotificationManager::class.java)
            notifManager.createNotificationChannel(channel)
        }
    }

}