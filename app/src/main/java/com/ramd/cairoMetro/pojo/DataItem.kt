package com.ramd.cairoMetro.pojo


import com.google.gson.annotations.SerializedName

data class DataItem(
    @SerializedName("coordinates")
    val coordinates: List<Double>,
    @SerializedName("intersection")
    val intersection: Boolean,
    @SerializedName("line")
    var line: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("neighbourStations")
    val neighbourStations: List<String>
)