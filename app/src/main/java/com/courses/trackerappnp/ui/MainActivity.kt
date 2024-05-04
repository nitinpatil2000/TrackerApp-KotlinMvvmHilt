package com.courses.trackerappnp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.courses.trackerappnp.R
import com.courses.trackerappnp.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //tell the compiler i have setup the custom toolbar
        setSupportActionBar(binding.toolbar)

        val navController =findNavController(R.id.fragmentContainerView)

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{
            _, destination, _ ->
            when(destination.id){
                R.id.runFragment, R.id.statisticsFragment, R.id.settingsFragment -> binding.bottomNavigation.visibility = View.VISIBLE
                else -> binding.bottomNavigation.visibility = View.GONE
            }
        }
    }
}