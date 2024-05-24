package com.courses.trackerappnp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.courses.trackerappnp.databinding.ItemRunBinding
import com.courses.trackerappnp.db.Run
import com.courses.trackerappnp.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(private val binding: ItemRunBinding) : ViewHolder(binding.root) {
        fun bind(run: Run) {
            binding.apply {
                Glide.with(itemView).load(run.img).into(ivRunImage)
                val calender = Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }

                val dateFormat = SimpleDateFormat("dd/mm/yy", Locale.getDefault())
                tvDate.text = dateFormat.format(calender.time)

                val avgSpeed = "${run.averageSpeedKMH}km/h"
                tvAvgSpeed.text = avgSpeed

                val distanceInKm = "${run.distanceInMeters / 1000f}km"
                tvDistance.text = distanceInKm

                tvTime.text = TrackingUtility.getFormattedStopwatchTime(run.timeInMillis)

                val caloriesBurned = "${run.caloriesBurned}kcal"
                tvCalories.text = caloriesBurned

            }
        }

    }

    private val _differItemCallBack = object : DiffUtil.ItemCallback<Run>() {

        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, _differItemCallBack)

    fun submitList(list: List<Run>) = differ.submitList(list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            ItemRunBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.bind(run)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}