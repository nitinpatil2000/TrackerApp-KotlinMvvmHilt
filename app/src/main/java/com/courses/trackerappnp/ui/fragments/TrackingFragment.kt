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
import com.courses.trackerappnp.other.TrackingUtility
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
    private var isTracking = false                                           //when your service is started or not.
    private var pathPoints = mutableListOf<Polyline>()

    //todo for the time
    private var currentTimeInMillis = 0L


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
            notifyForegroundService()
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolyline()
        }

        //todo observe the data in the tracking service
        subScribeObservers()
    }


    //todo this fun is only connect the two last polylines of our polyline list
    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {   //todo means at least two points then draw a line
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]      //get the first coordinate  (2 bcz at least pt. to connect line)
            val lastLatLng = pathPoints.last().last()                              // get the last coordinate
            val polylineOptions = PolylineOptions()                                //draw a single line
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)


            map?.addPolyline(polylineOptions)                                      //add the line on the map

        }
    }

    //todo this function draws all of our polylines on the map again when we rotate the device
    private fun addAllPolyline(){
        for (polyline in pathPoints){                            //get the loop and add the all polylines
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)                     //draw a multiple line
        }
    }


    //todo to show the line and zoom the camera immediately
    private fun updateCameraImmediately(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {        //means at least one point in present
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),                             //zoom the last point of the map
                    MAP_ZOOM
                )
            )
        }
    }

    //todo showing the view when the isTracking is true or false
    private fun updateViews(isTracking:Boolean){
        this.isTracking = isTracking
        if(!isTracking){                                            //false means the tracking is pause
            binding.apply {
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            }
        }else{
            binding.apply {
                btnToggleRun.text = "Stop"
                btnFinishRun.visibility = View.GONE
            }
        }
    }


    //todo notify the foreground service when it is paused or resume
    private fun notifyForegroundService(){
        if(isTracking){                                             //true means you can pause the tracking
            passedActionToService(ACTION_PAUSE_SERVICE)
        }else{                                                      //false means you can start or resume the service
            passedActionToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //todo notify the observers in the tracking service(isTracking, pathPoints) and update it
    private fun subScribeObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateViews(isTracking)
        }

        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()                                                   //when data is added then add the single line
            updateCameraImmediately()                                             //update the camera immediately
        }

        //todo for the time
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            val formatterTime = TrackingUtility.getFormattedStopwatchTime(currentTimeInMillis, true)
            binding.tvTimer.text = formatterTime
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