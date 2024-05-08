package com.courses.trackerappnp.di

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.courses.trackerappnp.R
import com.courses.trackerappnp.other.Constant.ACTION_SHOW_TRACKING_FRAGMENT
import com.courses.trackerappnp.other.Constant.NOTIFICATION_CHANNEL_ID
import com.courses.trackerappnp.ui.MainActivity
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
       @ApplicationContext context:Context
    ) = LocationServices.getFusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext context: Context
    ) = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideNotificationCompatBuilder(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)                                   //to prevent the notification is disappears when user click the notification
        .setOngoing(true)                                       //cant swap the notification away
        .setSmallIcon(R.drawable.ic_directions_run_black)
        .setContentTitle("Tracking App")
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)


}