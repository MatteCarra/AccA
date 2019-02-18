package mattecarra.accapp.adapters

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.parcel.Parcelize
import mattecarra.accapp.R
import java.util.ArrayList

@Parcelize
data class Schedule(val name: String, val executeOnce: Boolean, val hour: Int, val minute: Int, var command: String): Parcelable

class ScheduleRecyclerViewAdapter(val schedules: ArrayList<Schedule>, private val listener: (Schedule, Boolean) -> Unit) : RecyclerView.Adapter<ScheduleRecyclerViewAdapter.ScheduleViewHolder>() {

    fun saveState(bundle: Bundle) {
        bundle.putParcelableArrayList("schedules", schedules)
    }

    fun restoreState(bundle: Bundle) {
        bundle.getParcelableArrayList<Schedule>("schedules")?.let {
            schedules.addAll(it)
        }
    }

    fun add(schedule: Schedule) {
        schedules.add(schedule)
        notifyItemInserted(schedules.size - 1)
    }

    fun setList(schedules: List<Schedule>) {
        this.schedules.clear()
        this.schedules.addAll(schedules)
        notifyDataSetChanged()
    }

    fun remove(schedule: Schedule) {
        val index = schedules.indexOf(schedule)
        if(index != -1) {
            schedules.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getItem(index: Int): Schedule {
        return schedules[index]
    }

    fun size(): Int {
        return schedules.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleRecyclerViewAdapter.ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_row, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleRecyclerViewAdapter.ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    override fun getItemCount(): Int {
        return schedules.size
    }

    inner class ScheduleViewHolder(internal var view: View) : RecyclerView.ViewHolder(view) {
        fun bind(schedule: Schedule, listener: (Schedule, Boolean) -> Unit) = with(itemView) {
            view.findViewById<TextView>(R.id.title).text = resources.getString(R.string.schedule_row_text, if(!schedule.executeOnce) "Recurrent " else "", schedule.hour, schedule.minute)
            view.findViewById<ImageButton>(R.id.img_delete).setOnClickListener {
                listener(schedule, true)
            }
            setOnClickListener { listener(schedule, false) }
        }
    }
}