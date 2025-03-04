package com.ramd.cairoMetro.pojo

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri

class LocationCalculations {

    fun nearestLocation(data: Array<DataItem>, shortest:Float,lat: Double, long: Double): String {
        var shortestDistance = shortest
        var station = ""
        val stationCoordinates = data.map { it.coordinates }
        for (stationCoordinate in stationCoordinates) {
            val location1 = Location("")
            location1.latitude = stationCoordinate[0]
            location1.longitude = stationCoordinate[1]
            val location2 = Location("")
            location2.latitude = lat
            location2.longitude = long
            val distance = (location1.distanceTo(location2)) / 1000
            if (distance < shortestDistance) {
                shortestDistance = distance
                station = data.first { it.coordinates == stationCoordinate }.name
            }
        }
        return station
    }

    fun nearestStationPath(data: Array<DataItem>, shortest:Float , path:List<String>,lat: Double, long: Double):String{
        var shortestDistance = shortest
        var station = ""
        for (s in path) {
            val stationCoordinate = data.first { it.name == s }.coordinates
            val location1 = Location("")
            location1.latitude = stationCoordinate[0]
            location1.longitude = stationCoordinate[1]
            val location2 = Location("")
            location2.latitude = lat
            location2.longitude = long
            val distance = (location1.distanceTo(location2))
            if (distance <= shortestDistance) {
                shortestDistance = distance
                station = data.first { it.coordinates == stationCoordinate }.name
            }
        }

        return station
    }

    fun getLatAndLong(context: Context,address: String): Pair<Double, Double> {
        try {
            val coder = Geocoder(context)
            val addresses = coder.getFromLocationName(address, 1)

            if (!addresses.isNullOrEmpty()) {
                val lon = addresses[0].longitude;
                val lat = addresses[0].latitude
                return Pair(lat, lon)
            } else {
                return Pair(0.0, 0.0)
            }
        } catch (e: Exception) {
            return Pair(-1.0, -1.0)
        }
    }

    fun directionFromCurrentMap(destinationLatitude: String, destinationLongitude: String ,context: Context ) {
        val mapUri = Uri.parse("https://maps.google.com/maps?daddr=$destinationLatitude,$destinationLongitude")
        val a = Intent(Intent.ACTION_VIEW, mapUri)
        context.startActivity(a)
    }
}