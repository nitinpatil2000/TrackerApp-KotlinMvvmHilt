package com.courses.trackerappnp.repository

import com.courses.trackerappnp.db.Run
import com.courses.trackerappnp.db.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao
) {

    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByAverageSpeedKMH() = runDao.getAllRunsSortedByAverageSpeedKMH()

    fun getAllRunsSortedByDistanceInMeters() = runDao.getAllRunsSortedByDistanceInMeters()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getAllRunsSortedByCaloriesBurned()


    fun getTotalAverageSpeedKMH() = runDao.getTotalAverageSpeedKMH()

    fun getTotalDistanceInMeters() = runDao.getTotalDistanceInMeters()

    fun getTotalTimesInMillis() = runDao.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

}