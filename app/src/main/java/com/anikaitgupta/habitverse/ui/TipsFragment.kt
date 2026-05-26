package com.anikaitgupta.habitverse.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
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
import com.anikaitgupta.habitverse.databinding.FragmentTipsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class TipsFragment : Fragment() {
    private var _binding: FragmentTipsBinding? = null
    private val binding get() = _binding!!
    private val habitViewModel by activityViewModels<HabitViewModel>()
    private lateinit var adapter: TipsAdapter
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tips, container, false)
        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = TipsAdapter() { text ->
            textToSpeech.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                null
            )
        }
        binding.rvMessages.adapter = adapter

        val currentHabit = habitViewModel.habitUiState.value.currentEditHabit
        if (currentHabit != null) {
            if (habitViewModel.messages.value.isEmpty()) {
                habitViewModel.addUserInput("How to improve ${currentHabit.habitName}")
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    habitViewModel.messages.collectLatest { messages ->
                        adapter.submitList(messages)
                        if (messages.isNotEmpty()) {
                            binding.rvMessages.smoothScrollToPosition(messages.size - 1)
                        }
                    }
                }

                launch {
                    habitViewModel.geminiUiState.collect { uiState ->
                        when (uiState) {
                            is GeminiUiState.Idle -> {
                                binding.loadingLayout.visibility = View.GONE
                                binding.errorLayout.visibility = View.GONE
                            }
                            is GeminiUiState.Loading -> {
                                binding.loadingLayout.visibility = View.VISIBLE
                                binding.errorLayout.visibility = View.GONE
                            }
                            is GeminiUiState.Success -> {
                                binding.loadingLayout.visibility = View.GONE
                                binding.errorLayout.visibility = View.GONE
                            }
                            is GeminiUiState.Error -> {
                                binding.loadingLayout.visibility = View.GONE
                                binding.errorLayout.visibility = View.VISIBLE
                                binding.tvError.text = uiState.message
                            }
                        }
                    }
                }
            }
        }

        binding.btnRetry.setOnClickListener {
            habitViewModel.getTipsForHabitImprovement()
        }

        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                habitViewModel.addUserInput(userText)
                binding.etMessage.text?.clear()
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            habitViewModel.emptyMessagesList()
            habitViewModel.resetGeminiState()
            findNavController().navigateUp()
        }
        binding.btnSpeak.setOnClickListener {
            // Create an intent to start speech recognition
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            // Use free-form language model (natural speaking)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            // Set the language to the device's default
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            // Optional: prompt message shown in the recognition dialog
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")

            // Try to start the speech recognition activity
            try {
                startActivityForResult(intent, 1)
            } catch (e: Exception) {
                // Show error if recognizer activity is not found or fails
                Toast
                    .makeText(
                        requireContext(), " " + e.message,
                        Toast.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }
    // Handle the result from speech recognition activity
    @Deprecated("Deprecated in Java") // Still works, just marked as deprecated in newer APIs
    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if the result is from our speech input request
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                // Get the list of results from speech recognizer
                val result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                if(!result.isNullOrEmpty()) {
                    // Display the first recognized phrase in the TextView
                    val spokenText = result[0]
                    binding.etMessage.setText(spokenText)
                    binding.etMessage.setSelection(spokenText.length)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}