package com.ramd.cairoMetro.pojo

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.StationProgressBinding
import com.xwray.groupie.viewbinding.BindableItem

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
         }
        else if(end) {
             viewBinding.spStationName.text = "$station (end)"
             viewBinding.spStationName.setTextColor(Color.parseColor("#FEA613"))
             viewBinding.spline.setBackgroundResource(R.drawable.round_bottom)
         }
        else if (change)
        { viewBinding.spStationName.setTextColor(Color.parseColor("#5BB403"))
        viewBinding.spStationName.text = "$station (change)"
        }
    }

    override fun getLayout(): Int = R.layout.station_progress

    override fun initializeViewBinding(view: View): StationProgressBinding = StationProgressBinding.bind(view)
}