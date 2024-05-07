package com.courses.trackerappnp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.courses.trackerappnp.R
import com.courses.trackerappnp.databinding.FragmentTrackingBinding
import com.courses.trackerappnp.other.Constant.ACTION_PAUSE_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.courses.trackerappnp.other.Constant.MAP_ZOOM
import com.courses.trackerappnp.other.Constant.POLYLINE_COLOR
import com.courses.trackerappnp.other.Constant.POLYLINE_WIDTH
import com.courses.trackerappnp.service.Polyline
import com.courses.trackerappnp.service.TrackingService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions

class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private var map: GoogleMap? = null

    //todo for draw the polyline in the map
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToggleRun.setOnClickListener {
            passedActionToService(ACTION_START_OR_RESUME_SERVICE)
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
        }
    }


    //todo this fun is only connect the two last polylines of our polyline list
    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {   //todo we need to draw a line at least 2 points
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polyLineOptions)
        }
    }

    //todo this function draws all of our polylines on the map again when we rotate the device
    private fun addAllPolyline() {    //todo all polyline in the polylines.
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)
        }
    }

    //todo to show the line and zoom the camera immediately
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun updateTracking(isTracking:Boolean) {
        this.isTracking = isTracking

        if (!isTracking) {
            binding.apply {
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            }
        } else {
            binding.apply {
                btnToggleRun.text = "Stop"
                btnFinishRun.visibility = View.GONE
            }
        }
    }

    private fun toggleRun() {
        if (isTracking) {  //if the service is running then user can be paused this service
            passedActionToService(ACTION_PAUSE_SERVICE)
        } else {    //but if the service is paused then user can start or resume the service. 

        }
    }

    //todo use tracking service method pass the action.
    private fun passedActionToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        //not load the map everytime
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}