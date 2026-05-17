package com.example.habitverse.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.habitverse.R
import com.example.habitverse.databinding.FragmentSettingsBinding
import kotlin.getValue
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController

import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val habitViewModel by activityViewModels<HabitViewModel>()
    lateinit var binding: FragmentSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnDeleteAccount.setOnClickListener {
            // Handle account deletion logic
            AlertDialog.Builder(requireContext())
                .setTitle("Warning")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    // TODO: Implement account deletion logic via habitViewModel
                    onDelete()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        }
        binding.privacyPolicyCard.setOnClickListener {
            val website = "https://hyperskill.org".toUri()
            val intent = Intent(Intent.ACTION_VIEW, website)
            startActivity(intent)
        }
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
            habitViewModel.updateDeleteAccountStateToIdle()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                habitViewModel.deleteAccountState.collect { state ->
                    when (state) {
                        is DeleteAccountState.Success -> {
                            findNavController().navigate(R.id.auth_graph)
                            habitViewModel.updateDeleteAccountStateToIdle()
                        }
                        is DeleteAccountState.Error -> {
                            binding.tvError.visibility = View.VISIBLE
                            binding.tvError.text = state.message
                        }
                        else -> {
                            binding.tvError.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
    fun onDelete(){
        habitViewModel.deleteAccount()
    }

}