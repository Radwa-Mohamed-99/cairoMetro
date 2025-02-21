package com.ramd.cairoMetro.pojo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.StationProgressBinding
import com.xwray.groupie.viewbinding.BindableItem
import java.util.concurrent.TimeUnit

class StationItem(val station:String, val stationState: Boolean = false, val start: Boolean = false, val end: Boolean = false, val change: Boolean = false, val passed :Boolean =false): BindableItem<StationProgressBinding>() {
    override fun bind(viewBinding: StationProgressBinding, position: Int) {

        viewBinding.spStationName.setTextColor(Color.BLACK)
        viewBinding.spStationName.text = station
        viewBinding.spArrivedDot.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        viewBinding.spline.setBackgroundColor(Color.parseColor("#BAF18C"))
        viewBinding.spcard.setBackgroundColor (Color.WHITE)

        viewBinding.spStationName.text =station
        if(stationState) {
            viewBinding.spArrivedDot.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#FEA613"))
        }
        else if(passed) {
            viewBinding.spArrivedDot.backgroundTintList =
                ColorStateList.valueOf(Color.GRAY)
        }
         if(start) {
             viewBinding.spStationName.text = "$station (start)"
             viewBinding.spStationName.setTextColor(Color.parseColor("#FEA613"))
             viewBinding.spline.setBackgroundResource(R.drawable.round_top)
//             showNotification(viewBinding.root.context,station, "Start")
         }
        else if(end) {
             viewBinding.spStationName.text = "$station (end)"
             viewBinding.spStationName.setTextColor(Color.parseColor("#FEA613"))
             viewBinding.spline.setBackgroundResource(R.drawable.round_bottom)
//             showNotification(viewBinding.root.context,station, "End")
         }
        else if (change) {
            viewBinding.spStationName.setTextColor(Color.parseColor("#5BB403"))
            viewBinding.spStationName.text = "$station (change)"
//            showNotification(viewBinding.root.context,station, "Change")
        }

    }

    override fun getLayout(): Int = R.layout.station_progress

    override fun initializeViewBinding(view: View): StationProgressBinding = StationProgressBinding.bind(view)

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
        } else {





            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
                .putString("STATION_NAME", stationName)
                .putString("STATION_TYPE", stationType)
                .build()

            val request = OneTimeWorkRequest.Builder(MyWork::class.java)
                .setConstraints(constraints)
                .setInputData(
                    data
                )
                .setInitialDelay(
                    15,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueue(request)
        }
    }


}