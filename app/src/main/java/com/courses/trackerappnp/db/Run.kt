package com.courses.trackerappnp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run (
    var img:Bitmap ?=null,
    var timestamp:Long = 0L,  //convert this date into long and store it.
    var averageSpeedKMH: Float  = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L ,  //end time millis is very accurate.
    var caloriesBurned: Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id:Int? = null
}