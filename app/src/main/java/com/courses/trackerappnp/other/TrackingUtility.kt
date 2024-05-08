package com.courses.trackerappnp.other

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

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


    fun getFormattedStopwatchTime(ms: Long, isTracking: Boolean = false): String {
        var milliseconds = ms           //define the local variable bcz we need to calculate the hours, minutes,and seconds in the time
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.MILLISECONDS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MILLISECONDS.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        milliseconds -= TimeUnit.MILLISECONDS.toMillis(seconds)

        if (!isTracking) {
            return "${if (hours < 10) "0" else ""}$hours:" +          //return the time in HH:MM:SS if it is less than 10 simply append the 0 before the time(01)
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }

        //i want to show the milliseconds in 2 digits
        milliseconds /= 10          //ex milliseconds is 150 / 10 = 15

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"

    }
}
