package com.anikaitgupta.habitverse.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.anikaitgupta.habitverse.R
import com.anikaitgupta.habitverse.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    //lateinit var binding: FragmentLoginBinding
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel by activityViewModels<HabitViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //binding= DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment)
        }
        binding.btnLogin.setOnClickListener {
            if (!binding.etEmail.text.isNullOrEmpty() && !binding.etPassword.text.isNullOrEmpty()) {
                habitViewModel.login(
                    binding.etEmail.text.toString(), binding.etPassword.text.toString()
                )
            } else {
                Toast.makeText(
                    requireContext(), "Please enter email and password", Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.forgotPasswordFragment)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.loginState.collect { state ->
                    when (state) {
                        is LoginState.Success -> {
                            habitViewModel.clearRoomAndUpdateRoom()
                            findNavController().navigate(R.id.main_graph)
                            habitViewModel.updateLoginStateToIdle()
                        }

                        is LoginState.Error -> {
                            Toast.makeText(
                                requireContext(), state.message, Toast.LENGTH_SHORT
                            ).show()
                            habitViewModel.updateLoginStateToIdle()
                        }
                        else -> {}
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}