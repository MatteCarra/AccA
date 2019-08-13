package mattecarra.accapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.Schedule

class ScheduleRecyclerViewAdapter(private val listener: (Schedule, Boolean) -> Unit) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder>() {
    private var mSchedules = emptyList<Schedule>()

    fun getItem(index: Int): Schedule {
        return mSchedules[index]
    }

    override fun getItemCount(): Int {
        return mSchedules.size
    }

    fun setList(schedules: List<Schedule>) {
        if(mSchedules == schedules) {
            notifyDataSetChanged()
        } else {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return mSchedules.size
                }

                override fun getNewListSize(): Int {
                    return schedules.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return areContentsTheSame(oldItemPosition, newItemPosition)
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return schedules[newItemPosition] == mSchedules[oldItemPosition]
                }
            })
            mSchedules = schedules
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_row, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScheduleViewHolder(internal var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(schedule: Schedule) = with(itemView) {
            view.findViewById<TextView>(R.id.title).text = resources.getString(R.string.schedule_row_text, "Recurrent ", schedule.hour, schedule.minute)
            view.findViewById<ImageButton>(R.id.img_delete).setOnClickListener {
                listener(schedule, true)
            }
            setOnClickListener { listener(schedule, false) }
        }
    }
}