package com.example.habitverse.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.habitverse.R
import com.example.habitverse.data.Frequency
import com.example.habitverse.data.db.Habit
import com.example.habitverse.databinding.FragmentAddHabitBinding
import com.example.habitverse.databinding.FragmentMainScreenBinding
import com.example.habitverse.domain.HabitDomainModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddHabitFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class AddHabitFragment : Fragment() {
    // TODO: Rename and change types of parameters
    //private var param1: String? = null
    //private var param2: String? = null
    lateinit var binding: FragmentAddHabitBinding
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
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_add_habit, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        binding.toolbar.setNavigationOnClickListener {
            clickOnCancelOrBackButton(navController)
        }
        binding.bt1.setOnClickListener {
            clickOnCancelOrBackButton(navController)
        }
        /*if(habitViewModel._currentSelectedFrequencyAddFragment != null){
            val frequencyValue = habitViewModel._currentSelectedFrequencyAddFragment!!.frequency
            if(binding.autoCompleteTextView.text.toString() != frequencyValue){
                binding.autoCompleteTextView.setText(frequencyValue,false)
            }
        }*/
        //In case the person selected some frequency ans then rotated screen
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (habitViewModel.selectedFrequency.first() != null) {
                    val frequencyValue = habitViewModel.selectedFrequency.first()!!.frequency
                    if (binding.autoCompleteTextView.text.toString() != frequencyValue) {
                        binding.autoCompleteTextView.setText(frequencyValue, false)
                    }
                }
            }
        }
        /*binding.bt2.setOnClickListener {
            Log.d("TAG",binding.et1.text.isNullOrEmpty().toString())
            Log.d("TAG", habitViewModel._currentSelectedFrequencyAddFragment.toString())
            if(!binding.et1.text.isNullOrEmpty() && habitViewModel._currentSelectedFrequencyAddFragment != null){
                Log.d("TAG",binding.et1.text.isNullOrEmpty().toString())
                clickOnSaveButton(navController,binding.et1.text.toString(),
                    habitViewModel._currentSelectedFrequencyAddFragment!!
                )
            }
            else{
                Toast.makeText(requireContext(),"Please enter name and frequency", Toast.LENGTH_LONG).show()
            }
        }*/
        binding.bt2.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    if (!binding.et1.text.isNullOrEmpty() && habitViewModel.selectedFrequency.first() != null) {
                        val uiState = habitViewModel.habitUiState.first()
                        clickOnSaveButton(
                            navController,
                            binding.et1.text.toString(),
                            habitViewModel.selectedFrequency.first()!!
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please enter name and frequency",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    /*override fun onResume() {
        super.onResume()
        // get reference to the string array that we just created
        val frequency_values = resources.getStringArray(R.array.frequency_values)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_item, frequency_values)
        // get reference to the autocomplete text view
        val autocompleteTV = binding.autoCompleteTextView
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)
        binding.autoCompleteTextView.setOnItemClickListener {_, _, position, _ ->
            val selectedFrequency = Frequency.entries[position]
            habitViewModel.updateCurrentFrequencyAddFragment(selectedFrequency)
            Log.d("TAG", selectedFrequency.frequency)
        }
    }*/
    override fun onResume() {
        super.onResume()
        // get reference to the string array that we just created
        val frequency_values = resources.getStringArray(R.array.frequency_values)
        // create an array adapter and pass the required parameter
        // in our case pass the context, drop down layout , and array.
        val arrayAdapter = ArrayAdapter(this.requireContext(), R.layout.dropdown_item, frequency_values)
        // get reference to the autocomplete text view
        val autocompleteTV = binding.autoCompleteTextView
        // set adapter to the autocomplete tv to the arrayAdapter
        autocompleteTV.setAdapter(arrayAdapter)
        binding.autoCompleteTextView.setOnItemClickListener {_, _, position, _ ->
            val selectedFrequency = Frequency.entries[position]
            habitViewModel.updateCurrentFrequencyFragment(selectedFrequency)
        }
    }

    /*override fun onDestroyView() {
        super.onDestroyView()
        habitViewModel.updateCurrentFrequencyAddFragmentToNull()
    }

    fun clickOnCancelOrBackButton(navController: NavController){
        habitViewModel.updateCurrentFrequencyAddFragmentToNull()
        navController.navigateUp()
    }
    fun clickOnSaveButton(navController: NavController,habitName: String,habitFrequency: Frequency){
        habitViewModel.addHabit(Habit(habitName = habitName, habitFrequency = habitFrequency))
        habitViewModel.updateCurrentFrequencyAddFragmentToNull()
        navController.navigateUp()
    }*/
    override fun onDestroyView() {
        super.onDestroyView()
        habitViewModel.updateCurrentFrequencyFragmentToNull()
    }

    fun clickOnCancelOrBackButton(navController: NavController){
        habitViewModel.updateCurrentFrequencyFragmentToNull()
        navController.navigateUp()
    }
    fun clickOnSaveButton(navController: NavController,habitName: String,habitFrequency: Frequency){
        habitViewModel.addHabit(HabitDomainModel(habitName = habitName, habitFrequency = habitFrequency, remoteId = null))
        habitViewModel.updateCurrentFrequencyFragmentToNull()
        navController.navigateUp()
    }
    /*companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddHabitFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddHabitFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }*/
}