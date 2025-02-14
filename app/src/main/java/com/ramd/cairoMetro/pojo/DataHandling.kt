package com.ramd.cairoMetro.pojo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.core.content.edit
import com.google.gson.Gson
import java.io.InputStreamReader

class DataHandling {

    fun readUserFromAssets(context: Context, fileName:String): Array<DataItem>? {
        return try {
            val inputStream = context.assets.open(fileName)
            val inputStreamReader = InputStreamReader(inputStream)
            val gson = Gson()
            gson.fromJson(inputStreamReader, Array<DataItem>::class.java)
        } catch (e: Exception) {
            Log.e("JSON", "Error reading JSON file: ${e.message}")
            null
        }
    }

    fun getSimpleData( context: Context,key:String): Boolean {
        val file: SharedPreferences = context.getSharedPreferences("savedData", MODE_PRIVATE)
        val boolean = file.getBoolean(key, false)
        return boolean
    }
    fun saveSimpleData(context: Context,data:Boolean,key:String) {
        val file: SharedPreferences =context.getSharedPreferences("savedData", MODE_PRIVATE)
        file.edit {
            putBoolean(key, data)
        }
    }

    fun getListData( context: Context,key:String): Array<String> {
        val file: SharedPreferences = context.getSharedPreferences("savedData", MODE_PRIVATE)
        val gson = Gson()
        val json = file.getString(key, null)
        val list= gson.fromJson(json, Array<String>::class.java)
        if(!list.isNullOrEmpty())
        {
            return list
        }
        return emptyArray()
    }
    fun saveListData(context: Context,data:Array<String>,key:String) {
        val file: SharedPreferences =context.getSharedPreferences("savedData", MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(data)
        file.edit {
            putString(key, json)
        }
    }


}