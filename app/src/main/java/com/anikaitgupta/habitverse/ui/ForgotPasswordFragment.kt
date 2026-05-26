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
import com.anikaitgupta.habitverse.databinding.FragmentForgotPasswordBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [ForgotPasswordFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    // TODO: Rename and change types of parameters
    //lateinit var binding: FragmentForgotPasswordBinding
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel by activityViewModels<HabitViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_forgot_password, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendResetEmail.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isNotEmpty()) {
                habitViewModel.sendPasswordResetEmail(email)
                //binding.infoCard.visibility = View.VISIBLE
                //binding.btnSendResetEmail.isEnabled = false // prevent double-send
            }
            else{
                Toast.makeText(context, "Please enter an email", Toast.LENGTH_SHORT).show()
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.forgotPasswordState.collect { state ->
                    when (state) {
                        is ForgotPasswordState.Success -> {
                            binding.infoCard.visibility = View.VISIBLE
                            binding.btnSendResetEmail.isEnabled = false // prevent double-send
                        }

                        is ForgotPasswordState.Error -> {
                            binding.infoCard.visibility = View.VISIBLE
                            binding.btnSendResetEmail.isEnabled = true // prevent double-send
                            binding.tvInfoMessage.text = state.message

                        }

                        is ForgotPasswordState.Idle -> {
                            binding.infoCard.visibility = View.GONE
                            binding.btnSendResetEmail.isEnabled = true
                        }
                    }
                }
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        habitViewModel.updateForgotPasswordStateToIdle()
        _binding = null
    }
}