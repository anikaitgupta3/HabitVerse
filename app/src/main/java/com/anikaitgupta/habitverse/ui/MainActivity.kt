package com.anikaitgupta.habitverse.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import com.anikaitgupta.habitverse.R
import com.anikaitgupta.habitverse.databinding.ActivityMainBinding
import com.anikaitgupta.habitverse.domain.AuthRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var authRepository: AuthRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragmentContainerView)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        /*val navHostFragment =
        supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        if (savedInstanceState == null) {
            if (authRepository.checkLoggedIn()) {
                navController.setGraph(R.navigation.main_graph)
            } else {
                navController.setGraph(R.navigation.auth_graph)
            }
        }*/
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
                    as NavHostFragment

        val navController = navHostFragment.navController

        if (savedInstanceState == null) {

            val navGraph = navController.navInflater
                .inflate(R.navigation.root_nav_graph)

            if (authRepository.checkLoggedIn()) {
                navGraph.setStartDestination(R.id.main_graph)
            } else {
                navGraph.setStartDestination(R.id.auth_graph)
            }

            navController.graph = navGraph
        }
    }
}