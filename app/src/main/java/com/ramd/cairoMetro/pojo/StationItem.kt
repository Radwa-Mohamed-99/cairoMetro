package com.ramd.cairoMetro.pojo

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.StationProgressBinding
import com.xwray.groupie.viewbinding.BindableItem

class StationItem( val station:String ,
                   var stationState: Boolean = false,
                   var start: Boolean = false,
                   var end: Boolean = false,
                   var change: Boolean = false ,
                   var context: Context): BindableItem<StationProgressBinding>() {



    @SuppressLint("SetTextI18n")
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
         if(start) {
//             viewBinding.spStationName.text = "$station (start)"

             viewBinding.spStationName.text = context.getString(R.string.start2, station)
             viewBinding.spStationName.setTextColor(Color.parseColor("#FEA613"))
             viewBinding.spline.setBackgroundResource(R.drawable.round_top)
         }
        else if(end) {
//             viewBinding.spStationName.text = "$station (end)"
             viewBinding.spStationName.text = context.getString(R.string.end, station)

             viewBinding.spStationName.setTextColor(Color.parseColor("#FEA613"))
             viewBinding.spline.setBackgroundResource(R.drawable.round_bottom)
         }
        else if (change) {
            viewBinding.spStationName.setTextColor(Color.parseColor("#5BB403"))
//            viewBinding.spStationName.text = "$station (change)"
             viewBinding.spStationName.text = context.getString(R.string.change, station)
        }
    }

    override fun getLayout(): Int = R.layout.station_progress

    override fun initializeViewBinding(view: View): StationProgressBinding = StationProgressBinding.bind(view)
}