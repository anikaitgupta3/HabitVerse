package com.example.habitverse.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.habitverse.R
import com.example.habitverse.databinding.FragmentLoginBinding
import com.example.habitverse.databinding.FragmentRegistrationBinding
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
    lateinit var binding: FragmentRegistrationBinding
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
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
        binding.btnRegister.setOnClickListener {
            if (!binding.etRegEmail.text.isNullOrEmpty() && !binding.etRegPassword.text.isNullOrEmpty()) {

                habitViewModel.createAccount(
                    binding.etRegEmail.text.toString(),
                    binding.etRegPassword.text.toString()
                )
                viewLifecycleOwner.lifecycleScope.launch {

                    habitViewModel.registrationState.collect { state ->
                        when (state) {
                            is RegistrationState.Success -> {
                                findNavController().navigate(R.id.loginFragment)
                                habitViewModel.updateRegistrationStateToIdle()
                            }

                            is RegistrationState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT)
                                    .show()
                                habitViewModel.updateRegistrationStateToIdle()
                            }

                            else -> {}
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}