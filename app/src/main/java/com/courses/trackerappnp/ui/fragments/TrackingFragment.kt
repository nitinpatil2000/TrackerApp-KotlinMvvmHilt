package com.courses.trackerappnp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.courses.trackerappnp.R
import com.courses.trackerappnp.databinding.FragmentTrackingBinding
import com.courses.trackerappnp.db.Run
import com.courses.trackerappnp.other.Constant.ACTION_PAUSE_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_START_OR_RESUME_SERVICE
import com.courses.trackerappnp.other.Constant.ACTION_STOP_SERVICE
import com.courses.trackerappnp.other.Constant.MAP_ZOOM
import com.courses.trackerappnp.other.Constant.POLYLINE_COLOR
import com.courses.trackerappnp.other.Constant.POLYLINE_WIDTH
import com.courses.trackerappnp.other.TrackingUtility
import com.courses.trackerappnp.service.Polyline
import com.courses.trackerappnp.service.TrackingService
import com.courses.trackerappnp.ui.viewmodels.TrackingViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.math.round


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private lateinit var binding: FragmentTrackingBinding

    private var map: GoogleMap? = null

    //todo for draw the polyline in the map
    private var isTracking =
        false                                           //todo when your service is started or not.
    private var pathPoints = mutableListOf<Polyline>()

    //todo for the time
    private var currentTimeInMillis = 0L

    private var menu1: Menu? = null
    private var menuHost: MenuHost? = null

    private val weight = 60f

    private val trackingViewModel by viewModels<TrackingViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuHost = requireActivity()

        binding.btnToggleRun.setOnClickListener {
            notifyForegroundService()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholePoints()
            endRunAndSaveToDB()
        }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPolyline()
        }

        //todo observe the data in the tracking service
        subScribeObservers()

        //todo for show the menu in toolbar
        menuHost?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_tracking_menu, menu)
                menu1 = menu
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.npCancelTracking -> showDialogToStopTracking()
                }
                return true
            }
        })
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
        for (polyline in pathPoints) {                            //todo get the loop and add the all polylines
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)

            map?.addPolyline(polylineOptions)                     //todo draw a multiple line
        }
    }

    //todo to show the line and zoom the camera immediately
    private fun updateCameraImmediately() {
        if (pathPoints.isNotEmpty() && pathPoints.last()
                .isNotEmpty()
        ) {        //todo means at least one point in present
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last()
                        .last(),                             //todo zoom the last point of the map
                    MAP_ZOOM
                )
            )
        }
    }

    //todo showing the view when the isTracking is true or false
    private fun updateViews(isTracking:Boolean){
        this.isTracking = isTracking

        if(isTracking){
            binding.apply {
                btnToggleRun.text = "Stop"
                btnFinishRun.visibility = View.GONE
            }
        }else{
            binding.apply {
                btnToggleRun.text = "Start"
                btnFinishRun.visibility = View.VISIBLE
            }
        }
    }

    //todo notify the foreground service when it is paused or resume
    private fun notifyForegroundService() {
        if (isTracking) {                                             //todo true means you can pause the tracking
            menu1?.getItem(0)?.isVisible = true
            passedActionToService(ACTION_PAUSE_SERVICE)
        } else {                                                      //todo false means you can start or resume the service
            passedActionToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    //todo notify the observers in the tracking service(isTracking, pathPoints) and update it
    private fun subScribeObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner) {
            updateViews(it)
        }
        TrackingService.pathPoints.observe(viewLifecycleOwner) {
            pathPoints = it
            addLatestPolyline()                                                   //todo when data is added then add the single line
            updateCameraImmediately()                                             //todo update the camera immediately
        }

        //todo for the time
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner) {
            currentTimeInMillis = it
            val formatterTime = TrackingUtility.getFormattedStopwatchTime(currentTimeInMillis, true)
            if (currentTimeInMillis > 0L) {
                menu1?.getItem(0)?.isVisible = true
            }
            //todo show the time on the tvTimer view
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


    //todo for the cancel menu in toolbar
    private fun showDialogToStopTracking() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        passedActionToService(ACTION_STOP_SERVICE)
        menu1?.getItem(0)?.isVisible = false
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    //todo when i click the finish run button and show the all the points in center of the screenshot
    private fun zoomToSeeWholePoints(){
        val bounds = LatLngBounds.builder()                //todo store all the LatLng points
        for (polyline in pathPoints){
            for (points in polyline){
                bounds.include(points)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()         //todo show the points perfectly in center of the image
            )
        )
    }

    private fun endRunAndSaveToDB(){
        //todo take the screenshot of map
        map?.snapshot { bitmap ->

            var distanceInMeters = 0
            for(polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculateDistanceInMeterOfMap(polyline).toInt()
            }

            //todo ex distanceInMeters is 5000/1000.0 = 5  convert this distance in kilometer
            //todo currentTimeInMillis is 5000000/1000 to convert the seconds is 5000 / 60 = 83.33 minutes / 60 = 1.38 hours * 10 = 13.88 / 10.0 = 1.38
            //todo 5 / 1.388 = 3.68 is a averageSpeedKMH
            val averageSpeedKMH = round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis  //todo get the date in long and use the converter for store the date
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(bitmap, dateTimeStamp, averageSpeedKMH, distanceInMeters, currentTimeInMillis, caloriesBurned)
            trackingViewModel.insertRun(run)
            //todo show the message and destroy the fragment
            Snackbar.make(requireActivity().findViewById(R.id.rootView), "Run Saved Successfully", Snackbar.LENGTH_LONG).show()
            stopRun()
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
}