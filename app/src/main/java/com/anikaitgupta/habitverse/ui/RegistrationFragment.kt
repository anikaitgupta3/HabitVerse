package com.anikaitgupta.habitverse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anikaitgupta.habitverse.R
import com.anikaitgupta.habitverse.databinding.FragmentRegistrationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

/**
 * A simple [androidx.fragment.app.Fragment] subclass.
 * Use the [RegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RegistrationFragment : Fragment() {
    //lateinit var binding: FragmentRegistrationBinding
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel by activityViewModels<HabitViewModel>()
    // TODO: Rename and change types of parameters
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //binding= DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false)
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
        binding.toolbarReg.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnRegister.setOnClickListener {
            if (!binding.etRegEmail.text.isNullOrEmpty() && !binding.etRegPassword.text.isNullOrEmpty()) {
                habitViewModel.createAccount(
                    binding.etRegEmail.text.toString(), binding.etRegPassword.text.toString()
                )
            } else {
                Toast.makeText(
                    requireContext(), "Please enter email and password", Toast.LENGTH_SHORT
                ).show()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.registrationState.collect { state ->
                    when (state) {
                        is RegistrationState.Success -> {
                            findNavController().navigate(R.id.loginFragment)
                            habitViewModel.updateRegistrationStateToIdle()
                        }

                        is RegistrationState.Error -> {
                            Toast.makeText(
                                requireContext(), state.message, Toast.LENGTH_SHORT
                            ).show()
                            habitViewModel.updateRegistrationStateToIdle()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

}