package com.example.habitverse.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.habitverse.R
import com.example.habitverse.databinding.FragmentMainScreenBinding
import com.example.habitverse.domain.HabitDomainModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainScreenFragment : Fragment(), MenuProvider {

    /*companion object {
        fun newInstance() = MainScreenFragment()
    }*/

    //private val viewModel: MainScreenViewModel by viewModels()
    private val habitViewModel by activityViewModels<HabitViewModel>()
    lateinit var binding: FragmentMainScreenBinding
    lateinit var adapter: MainScreenAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setHasOptionsMenu(true)
        // TODO: Use the ViewModel


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //return inflater.inflate(R.layout.fragment_main_screen, container, false)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_screen, container, false)
        binding.rview.layoutManager = LinearLayoutManager(this.context)
        adapter = MainScreenAdapter({ habit ->
            onItemClick(habit)
        }, { habit, isChecked -> onCheckboxCheckedChanged(habit, !isChecked) })
        binding.rview.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkScheduleExactAlarmPermission()
        }
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
            habitViewModel.setPickedTime(10, 0)
        }
    }

    fun onItemClick(habit: HabitDomainModel) {
        habitViewModel.updateCurrentEditHabit(habit)
        habitViewModel.setPickedTime(
            habit.timeToShowNotification.hour,
            habit.timeToShowNotification.minute
        )
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

    fun onCheckboxCheckedChanged(habit: HabitDomainModel, isCurrentlyDone: Boolean) {
        habitViewModel.toggleCompletion(habit.id!!, isCurrentlyDone)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_logout -> {
                // Handle search action
                habitViewModel.syncAllPendingAndFailedHabits()
                //findNavController().setGraph(R.navigation.auth_graph)
                findNavController().navigate(R.id.auth_graph)
                habitViewModel.logout()
                habitViewModel.cleanRoom()
                true
            }

            else -> false
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::onNotificationPermissionResult
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                onNotificationPermissionGranted()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                showRationaleDialog()
            }

            else -> {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            onNotificationPermissionGranted()
        } else if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
            showSettingsDialog()
        }
        // else: denied without "Don't ask again" — do nothing
        else {
            //binding.cbReminder.isChecked = false
        }
    }

    private fun onNotificationPermissionGranted() {

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs notification permission to remind you about your habits.")
            .setPositiveButton("Grant") { _, _ ->
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel") { _, _ ->
                //binding.cbReminder.isChecked = false
            }
            .show()
    }

    private fun showSettingsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Notification permission was permanently denied. Please enable it in App Settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                        startActivity(intent)
                    }
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Cannot open settings", Toast.LENGTH_LONG)
                        .show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                //binding.cbReminder.isChecked = false
            }
            .show()
    }
//    @RequiresApi(Build.VERSION_CODES.S)
//    private val scheduleExactAlarmPermissionLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestPermission(),
//            ::onScheduleExactAlarmPermissionResult
//        )
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    fun checkScheduleExactAlarmPermission() {
//        when {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.SCHEDULE_EXACT_ALARM
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                //onNotificationPermissionGranted()
//                onScheduleExactAlarmPermissionGranted()
//            }
//            shouldShowRequestPermissionRationale(Manifest.permission.SCHEDULE_EXACT_ALARM) -> {
//                //showRationaleDialog()
//                showRationaleDialogForAlarmPermission()
//            }
//            else -> {
//                //notificationPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
//                scheduleExactAlarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
//            }
//        }
//    }

    //    @RequiresApi(Build.VERSION_CODES.S)
//    private fun onScheduleExactAlarmPermissionResult(granted: Boolean) {
//        if (granted) {
//            onScheduleExactAlarmPermissionGranted()
//        } else if (!shouldShowRequestPermissionRationale(Manifest.permission.SCHEDULE_EXACT_ALARM)) {
//            showSettingsDialogForAlarmPermission()
//        }
//        // else: denied without "Don't ask again" — do nothing
//        else{
//            //binding.cbReminder.isChecked = false
//        }
//    }
//
    private fun onScheduleExactAlarmPermissionGranted() {

    }

    //    @RequiresApi(Build.VERSION_CODES.S)
//    private fun showRationaleDialogForAlarmPermission() {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Permission Required")
//            .setMessage("This app requires schedule exact alarm permission to remind you about your habits.")
//            .setPositiveButton("Grant") { _, _ ->
//                scheduleExactAlarmPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM)
//            }
//            .setNegativeButton("Cancel") { _, _ ->
//                //binding.cbReminder.isChecked = false
//            }
//            .show()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.S)
//    private fun showSettingsDialogForAlarmPermission() {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle("Permission Required")
//            .setMessage("Schedule exact alarm permission was permanently denied. Please enable it in App Settings.")
//            .setPositiveButton("Open Settings") { _, _ ->
//                try {
////                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
////                        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
////                        startActivity(intent)
////                    }
//                    startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
//                } catch (e: ActivityNotFoundException) {
//                    Toast.makeText(requireContext(), "Cannot open settings", Toast.LENGTH_LONG).show()
//                }
//            }
//            .setNegativeButton("Cancel") { _, _ ->
//                // binding.cbReminder.isChecked = false
//            }
//            .show()
//    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun checkScheduleExactAlarmPermission() {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (alarmManager.canScheduleExactAlarms()) {
            // Permission already granted
            onScheduleExactAlarmPermissionGranted()
        } else {
            // Can't request normally — must direct to settings
            showRationaleDialogForAlarmPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showRationaleDialogForAlarmPermission() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app requires the exact alarm permission to remind you about your habits.")
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.fromParts(
                            "package",
                            requireActivity().packageName,
                            null
                        ) // ✅ pass package URI
                    ).also { startActivity(it) }
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Cannot open settings", Toast.LENGTH_LONG)
                        .show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager =
                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                onScheduleExactAlarmPermissionGranted()
            }
        }
    }

}