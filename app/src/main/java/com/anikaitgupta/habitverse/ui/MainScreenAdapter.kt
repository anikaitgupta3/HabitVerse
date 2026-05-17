package com.anikaitgupta.habitverse.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anikaitgupta.habitverse.R
import com.anikaitgupta.habitverse.databinding.ListItemBinding
import com.anikaitgupta.habitverse.domain.HabitDomainModel

class MainScreenAdapter(/*habits: List<Habit>,*/private val onHabitClick: (HabitDomainModel) -> Unit, private val onCheckboxCheckedChanged:(HabitDomainModel, Boolean)-> Unit): ListAdapter<HabitDomainModel,HabitViewHolder> (
    object : DiffUtil.ItemCallback<HabitDomainModel>() {
        override fun areItemsTheSame(
            oldItem: HabitDomainModel,
            newItem: HabitDomainModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HabitDomainModel, newItem: HabitDomainModel): Boolean =
            oldItem == newItem
    }) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitViewHolder {
        //TODO("Not yet implemented")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item, parent, false)
        return HabitViewHolder(binding,onHabitClick,onCheckboxCheckedChanged)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /*init {
        submitList(habits)
    }*/

}

class HabitViewHolder(
    private val binding: ListItemBinding,
    private val onHabitClick: (HabitDomainModel) -> Unit,
    private val onCheckboxCheckedChanged:(HabitDomainModel, Boolean)-> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(habit: HabitDomainModel) {
        binding.tvHabitTitle.text = habit.habitName
        binding.tvHabitFrequency.text = habit.habitFrequency.frequency
        binding.tvHabitTime.text = habit.timeToShowNotification.toString()
        // 1. Clear the listener BEFORE setting the state
        binding.cbHabitStatus.setOnCheckedChangeListener(null)
        binding.cbHabitStatus.isChecked = habit.isCompleted

        itemView.setOnClickListener {
            onHabitClick(habit)
        }
        binding.cbHabitStatus.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                onCheckboxCheckedChanged(habit,true)
            }
            else{
                onCheckboxCheckedChanged(habit,false)
            }
        }
    }
}
