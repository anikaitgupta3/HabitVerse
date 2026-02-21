package com.example.habitverse.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitverse.R
import com.example.habitverse.data.Habit
import com.example.habitverse.databinding.FragmentMainScreenBinding
import com.example.habitverse.domain.HabitDomainModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue
@AndroidEntryPoint
class MainScreenFragment : Fragment() {

    /*companion object {
        fun newInstance() = MainScreenFragment()
    }*/

    //private val viewModel: MainScreenViewModel by viewModels()
    private val habitViewModel by activityViewModels<HabitViewModel>()
    lateinit var binding: FragmentMainScreenBinding
    lateinit var adapter: MainScreenAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return inflater.inflate(R.layout.fragment_main_screen, container, false)
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_main_screen, container, false)
        binding.rview.layoutManager = LinearLayoutManager(this.context)
        adapter = MainScreenAdapter(){
            habit -> onItemClick(habit)
        }
        binding.rview.adapter=adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Add bottom padding to RecyclerView
            binding.rview.updatePadding(
                bottom = systemBars.bottom
            )

            // Move FAB above nav bar
            binding.fab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + resources.getDimensionPixelSize(R.dimen.fab_margin)
            }

            insets
        }*/


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.habitUiState.collect { uiState ->
                    adapter.submitList(uiState.listOfHabits)
                }
            }
        }
        binding.rview.addItemDecoration(
            DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(R.drawable.divider.toDrawable())
            }
        )
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.addHabitFragment)
        }
    }
    fun onItemClick(habit: HabitDomainModel){
        habitViewModel.updateCurrentEditHabit(habit)
        findNavController().navigate(R.id.editHabitFragment)
        //val bundle = bundleOf("Key" to habit.id)
        //findNavController().navigate(R.id.editHabitFragment,bundle)
        /*viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val uiState=habitViewModel.habitUiState.first()
                Log.d("TAG", uiState.currentEditHabit.toString())
            }
        }*/
    }

}