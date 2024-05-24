package com.courses.trackerappnp.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import com.courses.trackerappnp.service.Polyline
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

object TrackingUtility {

    //    todo android android sdk is less than api level 29 then ask fine and coarse location if the sdk is higher than the api level 29
//    todo then ask the background, fine, coarse location.
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


    fun getFormattedStopwatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms           //todo define the local variable bcz we need to calculate the hours, minutes,and seconds in the time
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)

        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)

        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)


        if (!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +         //todo return the time in HH:MM:SS if it is less than 10 simply append the 0 before the time(01)
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }

        //todo i want to show the milliseconds in 2 digits

        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10          //todo ex milliseconds is 150 / 10 = 15

        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }


    //todo this function calculate the distance between the all points
    fun calculateDistanceInMeterOfMap(polyline: Polyline): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val position1 = polyline[i]
            val position2 = polyline[i + 1]

            val result = FloatArray(1)

            Location.distanceBetween(                           //todo this function sum of the all distance return the result as you expect in float
                position1.latitude,
                position1.longitude,
                position2.latitude,
                position2.longitude,
                result
            )

            distance += result[0]
        }

        return distance
    }
}
