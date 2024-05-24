package com.courses.trackerappnp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.courses.trackerappnp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: MainRepository
): ViewModel() {



}