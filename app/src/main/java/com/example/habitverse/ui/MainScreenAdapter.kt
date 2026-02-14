package com.example.habitverse.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitverse.R
import com.example.habitverse.data.Habit
import com.example.habitverse.databinding.ListItemBinding

class MainScreenAdapter(/*habits: List<Habit>,*/private val onHabitClick: (Habit) -> Unit): ListAdapter<Habit,HabitViewHolder> (
    object : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(
            oldItem: Habit,
            newItem: Habit
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean =
            oldItem == newItem
    }) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitViewHolder {
        //TODO("Not yet implemented")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item, parent, false)
        return HabitViewHolder(binding,onHabitClick)
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
    private val onHabitClick: (Habit) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(habit: Habit) {
        binding.tv1.text = habit.habitName
        binding.tv3.text = habit.habitFrequency.frequency

        itemView.setOnClickListener {
            onHabitClick(habit)
        }
    }
}
