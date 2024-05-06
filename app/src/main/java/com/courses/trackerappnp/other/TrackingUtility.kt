package com.courses.trackerappnp.other

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_ID
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_NAME
import pub.devrel.easypermissions.EasyPermissions

object TrackingUtility {

    //    //android android sdk is less than api level 29 then ask fine and coarse location if the sdk is higher than the api level 29
//    //then ask the background, fine, coarse location.
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun hasLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
}
