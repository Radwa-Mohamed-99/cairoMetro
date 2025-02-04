package com.ramd.cairoMetro.ui

import StationsCount
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ramd.cairoMetro.R
import com.ramd.cairoMetro.databinding.ActivityMainBinding
import com.ramd.cairoMetro.pojo.CalculatePrice
import com.ramd.cairoMetro.pojo.Direction
import com.ramd.cairoMetro.pojo.MetroLines
import com.ramd.cairoMetro.pojo.Paths

class MainActivity : AppCompatActivity(), OnClickListener {
    lateinit var file: SharedPreferences
    lateinit var binding: ActivityMainBinding
    var start = ""
    var arrival = ""
    var stationCount = 0

    lateinit var metroLines: MetroLines
    lateinit var path: Paths
    lateinit var calculatePrice: CalculatePrice
    lateinit var direction: Direction
    lateinit var stationsCount: StationsCount

    var indexPlus = 0
    var indexMins = 0

    val paths = mutableListOf<List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        metroLines = MetroLines()
        path = Paths(metroLines)
        calculatePrice = CalculatePrice()
        direction = Direction(metroLines)
        stationsCount = StationsCount(path)

        binding.getDetail.setOnClickListener(this)
        binding.perviousPath.setOnClickListener(this)
        binding.shortestPath.setOnClickListener(this)
        binding.nextPath.setOnClickListener(this)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun validateStations(): Boolean {
        return when {
            binding.startStation.selectedItemPosition == 0 || binding.arrivalStation.selectedItemPosition == 0 -> {
                showToast("Select a station")
                false
            }

            binding.startStation.selectedItem == binding.arrivalStation.selectedItem -> {
                showToast("Arrival and start station can't be the same")
                false
            }

            else -> true
        }
    }

    private fun calculateTime(count: Int) {
        val time = "${(count * 3) / 60} hrs ${(count * 3) % 60} mins"
        binding.time.text = "Time \n $time"
    }

    private fun updateShortestPathUI() {
//        val shortPath = path.findShortPath(start, arrival)
        val shortPath = path.findShortPathLessInterSection(start,arrival)
        if (shortPath.isNotEmpty()) {
            binding.station.text = "The Shortest Path:\n  ${shortPath.joinToString(",")}"
            binding.direction.text =
                "Direction: \n " + direction.direction(shortPath, metroLines.lines)
            stationCount = stationsCount.stationCountOfShort(start, arrival)
            binding.stationNo.text = "Station NO \n $stationCount "
            calculateTime(stationCount)
        }
    }

    private fun updatePathUI(path: List<String>) {
        binding.station.text = "Another Path:\n  ${path.joinToString(",")}"
        binding.direction.text = "Direction: \n " + direction.direction(path, metroLines.lines)
        stationCount = stationsCount.stationCountOfAllPaths(paths, indexMins)
        binding.stationNo.text = "Station NO \n $stationCount "
        calculateTime(stationCount)
    }

    fun getDetails() {
        if (!validateStations()) return
        start = binding.startStation.selectedItem.toString().lowercase()
        arrival = binding.arrivalStation.selectedItem.toString().lowercase()
        indexPlus = 0
        indexMins = 0
        paths.clear()

        binding.nextPath.isEnabled = false
        binding.perviousPath.isEnabled = false
        binding.shortestPath.isEnabled = false
        binding.count.text = ""

        val allPaths = path.findAllPaths(start, arrival)
        val shortPath = path.findShortPath(start, arrival)

        updateShortestPathUI()

        val tripPrice = calculatePrice.calculatePrice(stationCount)
        binding.price.text = "Price \n$tripPrice"

        if (allPaths.size > 1) {
            paths.addAll(allPaths.filter { it != shortPath })
            binding.count.text = "${0} / ${paths.size}"
            binding.nextPath.isEnabled = true
        }
    }

    fun shortest() {
        binding.count.text = "0 / ${paths.size}"
        updateShortestPathUI()
    }

    fun next() {
        updatePathUI(paths[indexPlus])
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

    fun pervious(){
        binding.nextPath.isEnabled = true;
        indexPlus = indexMins
        indexMins--
        binding.count.text = "${indexMins} / ${paths.size}"
        updatePathUI(paths[indexMins])
        if (indexMins <= 0) {
            binding.perviousPath.isEnabled = false
            indexPlus = 1
            return
        }
    }

    fun swapStations() {
        val startPos = binding.startStation.selectedItemPosition
        val arrivalPos = binding.arrivalStation.selectedItemPosition

        binding.startStation.setSelection(arrivalPos)
        binding.arrivalStation.setSelection(startPos)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.perviousPath -> pervious()
            R.id.shortestPath -> shortest()
            R.id.nextPath -> next()
            R.id.getDetail -> getDetails()
        }
    }
}


