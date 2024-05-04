package com.courses.trackerappnp.service

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.courses.trackerappnp.other.Constant.ACTION_PAUSE_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_STOP_SERVICE
import timber.log.Timber

class TrackingService : LifecycleService() {

    //perform 3 action first is service start or resume, second is pause, third is stop.
    //when onStartCommand function start the service and handled the action passed to the activity or fragment.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //when my intent data is not null then perform the action
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE ->
                    Timber.d("Service is started or resume")
                ACTION_PAUSE_SERVICE ->
                    Timber.d("Service is paused")
                ACTION_STOP_SERVICE ->
                    Timber.d("Service is stopped")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
}