package com.example.habitverse.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.habitverse.R
import com.example.habitverse.databinding.FragmentAddHabitBinding
import com.example.habitverse.databinding.FragmentTipsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [TipsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TipsFragment : Fragment() {
    lateinit var binding: FragmentTipsBinding
    private val habitViewModel by activityViewModels<HabitViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_add_habit, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tips, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val currentHabit = habitViewModel.habitUiState.value.currentEditHabit ?: return
        binding.tvQuestion.text = "How to improve ${currentHabit.habitName}"

        val prompt = """
        Habit: ${currentHabit.habitName}
        
        Give 3 short practical tips 
        to improve this habit.
        
        Keep response under 80 words.
    """.trimIndent()

        habitViewModel.getTipsForHabitImprovement(prompt)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                habitViewModel.geminiUiState.collect { uiState ->
                    when (uiState) {
                        is GeminiUiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.emptyState.visibility = View.GONE
                            binding.tvAnswer.visibility = View.VISIBLE
                            binding.tvAnswer.text = "${uiState.message} Please try again later."
                        }

                        is GeminiUiState.Idle -> {
                            binding.progressBar.visibility = View.GONE
                            binding.emptyState.visibility = View.GONE
                            binding.tvAnswer.visibility = View.GONE
                        }

                        is GeminiUiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.emptyState.visibility = View.VISIBLE
                            binding.tvAnswer.visibility = View.GONE
                        }

                        is GeminiUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.emptyState.visibility = View.GONE
                            binding.tvAnswer.visibility = View.VISIBLE
                            binding.tvAnswer.text = uiState.data
                        }
                    }
                }
            }
        }
        binding.toolbar.setNavigationOnClickListener {
            habitViewModel.resetGeminiState()
            findNavController().navigateUp()
        }
    }
}