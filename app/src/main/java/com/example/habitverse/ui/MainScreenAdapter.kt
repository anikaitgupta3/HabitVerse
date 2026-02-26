package com.example.habitverse.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.habitverse.R
import com.example.habitverse.data.db.Habit
import com.example.habitverse.databinding.ListItemBinding
import com.example.habitverse.domain.HabitDomainModel

class MainScreenAdapter(/*habits: List<Habit>,*/private val onHabitClick: (HabitDomainModel) -> Unit): ListAdapter<HabitDomainModel,HabitViewHolder> (
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
    private val onHabitClick: (HabitDomainModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(habit: HabitDomainModel) {
        binding.tv1.text = habit.habitName
        binding.tv3.text = habit.habitFrequency.frequency

        itemView.setOnClickListener {
            onHabitClick(habit)
        }
    }
}
