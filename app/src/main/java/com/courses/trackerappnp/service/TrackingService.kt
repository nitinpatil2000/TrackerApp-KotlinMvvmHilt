package com.courses.trackerappnp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.courses.trackerappnp.R
import com.courses.trackerappnp.other.Constant.ACTION_PAUSE_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_SHOW_TRACKING_FRAGMENT
import com.courses.trackerappnp.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_STOP_SERVICE
import com.courses.trackerappnp.other.Constant.FASTEST_LOCATION_INTERVAL
import com.courses.trackerappnp.other.Constant.LOCATION_UPDATE_INTERVAL
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_ID
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_NAME
import com.courses.trackerappnp.other.Constant.NOTIFICATION_ID
import com.courses.trackerappnp.other.TrackingUtility
import com.courses.trackerappnp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

//todo used in the pathPoints i.e., ShortCut
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

class TrackingService : LifecycleService() {
    private var isStartRun = true
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()                //showing this time in seconds in notification

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()

        //todo use this variable in the tracking fragment using class name so define in companion object
        val timeRunInMillis = MutableLiveData<Long>()
    }


    private fun postInitialValue() {
        //todo pass the initial value when application is launched otherwise it will throw the null pointer exception.
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())

        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //todo it is only run when it is true
        isTracking.observe(this) {
            updateLocationTracking(it)
        }
    }


    //perform 3 action first is service start or resume, second is pause, third is stop.
    //when onStartCommand function start the service and handled the action passed to the activity or fragment.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE ->
                    if (isStartRun) {                                          //when it is start
                        startForegroundService()
                        isStartRun = false
                    } else {                                                  //when it is resumed
                        Timber.d("Service is Running...")
                        startTimer()
                    }
                ACTION_PAUSE_SERVICE -> {                                      //when it is paused
                    Timber.d("Service is paused")
                    pausedService()
                }

                ACTION_STOP_SERVICE ->
                    Timber.d("Service is stopped")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun pausedService(){
        isTracking.postValue(false)
        isTimeEnabled = false
    }

    /*
  todo step 1 is create notification channel
  todo step 2 is start foreground service using notification Builder define this builder in the service module file using dagger.
  todo step 3 create the pending intent using dagger hilt in the service module file.
  todo step 4 define the global action in the navGraph file
  todo step 5 handled this event in the main activity.
   */
    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        //todo call the timer funtion
        startTimer()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)  //to prevent the notification is disappears when user click the notification
            .setOngoing(true) //cant swap the notification away
            .setSmallIcon(R.drawable.ic_directions_run_black)
            .setContentTitle("Tracking App")
            .setContentText("00:00:00")
//            todo final is pending intent to open the the tracking fragment and attached the action.
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }


    private fun getMainActivityPendingIntent() =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT         //when we launch pending intent and it is already exist then update this pending intent.
        )


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW //todo cannot give the sound when the notification is occur
        )
        notificationManager.createNotificationChannel(channel)
    }




    /*
     todo get the location in the google map
        todo step 1 - define the variables in the companion object to check tracking is pause or start or add the location.
        todo step 2- postInitialValue in the variables (empty)
        todo step 3- addEmptyPolyline from the beginning. because it is a list of list of lat-lng
        todo step 4- call this empty polyline fun in this foreground service method
        todo step 5 - add the location in the path-points (list of coordinates)
        todo step 6 - add the list of polyline in the path points using the location result override function
                     todo because it is a list of polyline and (list of coordinate).
      */

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    //todo remember path points is not null in this function so call this path points using let block in this location callback.
    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }


    //todo in this method add the polylines in the path points
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }


    //todo if the isTracking is false then stop the location tracking and it is true when start the location.
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    /*
    todo define the time whether it is start or not(boolean)
    todo define the time from the beginning service (start ,resume, pause)
    todo define the total time
    todo define the define the timestamp when we started
    todo define the last second time stamp because i add the 1000L millis in this lastTimeStamp and compare the value
    todo defint the timer function
     */


    private var isTimeEnabled = false
    private var timeStarted = 0L
    private var beginningTime = 0L
    private var totalTimeRun = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        isTimeEnabled = true
        timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!){
            //calculate the difference between current and previous started time ex stopwatch start at 1 seconds and second time start at 5 second.(1-5)
                beginningTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(totalTimeRun + beginningTime)

                if(timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){         //greater than or equal to 1 seconds then update the counter
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(50L)
            }
            //add the the current beginning time in the total-time run and add the value in this time run in milliseconds
            totalTimeRun += beginningTime
        }
    }
}