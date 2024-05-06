package com.courses.trackerappnp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.courses.trackerappnp.R
import com.courses.trackerappnp.other.Constant.ACTION_PAUSE_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_SHOW_TRACKING_FRAGMENT
import com.courses.trackerappnp.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_STOP_SERVICE
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_ID
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_NAME
import com.courses.trackerappnp.other.Constant.NOTIFICATION_ID
import com.courses.trackerappnp.ui.MainActivity
import timber.log.Timber

class TrackingService : LifecycleService() {
    private var isStartRun = false

    //perform 3 action first is service start or resume, second is pause, third is stop.
    //when onStartCommand function start the service and handled the action passed to the activity or fragment.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE ->
                    if(!isStartRun){
                        startForegroundService()
                        isStartRun = true
                    }else{
                        Timber.d("Service is Running...")
                    }

                ACTION_PAUSE_SERVICE ->
                    Timber.d("Service is paused")

                ACTION_STOP_SERVICE ->
                    Timber.d("Service is stopped")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    /*
    todo step 1 is create notification channel
    todo step 2 is start foreground service using notification Builder
    todo step 3 create the pending intent to open the current activity
    todo step 4 define the global navGraph
    todo step 5 handled this event in the main activity.
     */

    private fun startForegroundService() {
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

}