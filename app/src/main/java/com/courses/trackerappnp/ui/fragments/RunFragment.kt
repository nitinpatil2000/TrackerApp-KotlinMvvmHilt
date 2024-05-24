package com.courses.trackerappnp.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.courses.trackerappnp.R
import com.courses.trackerappnp.adapter.RunAdapter
import com.courses.trackerappnp.databinding.FragmentRunBinding
import com.courses.trackerappnp.other.Constant.REQUEST_CODE_LOCATION_PERMISSION
import com.courses.trackerappnp.other.TrackingUtility
import com.courses.trackerappnp.ui.viewmodels.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {
    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!
    private lateinit var runAdapter: RunAdapter
    private val viewModel by viewModels<TrackingViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRunBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()

        setUpRecyclerView()

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }

        viewModel.runSortByDate.observe(viewLifecycleOwner){
            runAdapter.submitList(it)
        }
    }

    private fun setUpRecyclerView(){
        runAdapter = RunAdapter()
        binding.rvRuns.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = runAdapter
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        //call this utility function for the permission.
        if(TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        //if deny this location permission then show this message.
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this app.",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }


    //if the Permission deny permanently the then show the alert dialog box and move to the setting app
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermission()
        }
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}