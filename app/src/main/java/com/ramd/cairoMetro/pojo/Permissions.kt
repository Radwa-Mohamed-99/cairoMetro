package com.ramd.cairoMetro.pojo

import android.Manifest
import android.Manifest.*
import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class Permissions (val context: Context) {

    private val requiredPermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            // Android 13+ requires specific foreground service permission
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                ACCESS_BACKGROUND_LOCATION,
                FOREGROUND_SERVICE,
                POST_NOTIFICATIONS,
                FOREGROUND_SERVICE_LOCATION
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            // Android 10-12
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
                ACCESS_BACKGROUND_LOCATION,
                POST_NOTIFICATIONS,
                FOREGROUND_SERVICE
            )
        }
        else -> {
            // Android 9 and below
            arrayOf(
                ACCESS_FINE_LOCATION,
                ACCESS_COARSE_LOCATION,
            )
        }
    }



    fun hasRequiredPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

     fun requestLocationPermissions(permissionLauncher:ActivityResultLauncher<Array<String>>) {
        // For Android 10+ (API 29+), request foreground permissions first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundPermissions = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        FOREGROUND_SERVICE,
                        POST_NOTIFICATIONS,
                        FOREGROUND_SERVICE_LOCATION
                    )
                }
                else -> {
                    arrayOf(
                        ACCESS_FINE_LOCATION,
                        ACCESS_COARSE_LOCATION,
                        FOREGROUND_SERVICE
                    )
                }
            }

            permissionLauncher.launch(foregroundPermissions)

            // After granting foreground permission, request background permission separately
            if (foregroundPermissions.all {
                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                }) {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
        } else {
            // For older Android versions
            permissionLauncher.launch(requiredPermissions)
        }
    }

}