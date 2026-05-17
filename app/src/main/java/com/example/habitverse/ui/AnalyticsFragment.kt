package com.example.habitverse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.example.habitverse.R
import com.example.habitverse.databinding.FragmentAnalyticsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {

    private lateinit var binding: FragmentAnalyticsBinding
    private val habitViewModel by activityViewModels<HabitViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analytics, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            MaterialTheme {
                val uiState by habitViewModel.analyticsUiState.collectAsStateWithLifecycle()
                AnalyticsScreen(uiState,{findNavController().navigateUp()})
            }
        }
    }
}