package com.courses.trackerappnp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.courses.trackerappnp.R
import com.courses.trackerappnp.databinding.ActivityMainBinding
import com.courses.trackerappnp.other.Constant.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //tell the compiler i have setup the custom toolbar
        setSupportActionBar(binding.toolbar)

        navController = findNavController(R.id.fragmentContainerView)

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.runFragment, R.id.statisticsFragment, R.id.settingsFragment -> binding.bottomNavigation.visibility =
                    View.VISIBLE

                else -> binding.bottomNavigation.visibility = View.GONE
            }
        }

        openTrackingFragmentWhenClickNotification(intent)

    }

    // The onNewIntent() method is called on the existing activity instance to handle the new intent.
    // when application is running so click the notification so it cannot recreate the activity used this instance of the activity.
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        openTrackingFragmentWhenClickNotification(intent)
    }

    //when user click the notification then open the tracking fragment so define the global action in the navigation graph
    //and define the constant in the constant file and attached the action the tracking service.
    private fun openTrackingFragmentWhenClickNotification(intent: Intent?){
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            if(::navController.isInitialized){
                navController.navigate(R.id.action_global_tracking_fragment)
            }
        }
    }

}
