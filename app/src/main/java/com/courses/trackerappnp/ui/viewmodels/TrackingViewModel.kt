package com.courses.trackerappnp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.courses.trackerappnp.db.Run
import com.courses.trackerappnp.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    fun insertRun(run: Run) =
        viewModelScope.launch {
            repository.insertRun(run)
        }

    val runSortByDate = repository.getAllRunsSortedByDate()
}