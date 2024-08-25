package com.io1.bitirmeprojesi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.io1.bitirmeprojesi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = binding.navViewBottom

        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener{controller, destination, argument ->
            when(destination.id){
                R.id.photoFragment,R.id.cameraFragment,R.id.historyFragment ->{
                    binding.navViewBottom.visibility = View.VISIBLE
                }
                R.id.login, R.id.register->{
                    binding.navViewBottom.visibility = View.GONE
                }
            }
        }
    }

}