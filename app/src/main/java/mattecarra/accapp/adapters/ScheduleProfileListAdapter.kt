package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.models.Schedule

interface OnScheduleClickListener {
    fun onScheduleProfileClick(schedule: Schedule)
    fun onScheduleDeleteClick(schedule: Schedule)
}

class ScheduleProfileListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<ScheduleProfileListAdapter.ProfileViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mScheduleList = emptyList<Schedule>()
    private lateinit var mListener: OnScheduleClickListener
    private val mContext = context

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        override fun onClick(v: View?) {
            mListener.onScheduleProfileClick(mScheduleList[adapterPosition])
        }

        init {
            itemView.setOnClickListener(this)
        }

        val titleTv: TextView = itemView.findViewById(R.id.item_schedule_title_tv)
        val profileTv: TextView = itemView.findViewById(R.id.item_schedule_profile_tv)
        val whenTv: TextView = itemView.findViewById(R.id.item_schedule_when_tv)
        val optionsIb: ImageButton = itemView.findViewById(R.id.item_schedule_options_ib)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val itemView = mInflater.inflate(R.layout.schedule_item, parent, false)
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val schedule = mScheduleList[position]
        val time = schedule.getTime()

        when {
            time != null && schedule.executeOnce -> {
                holder.titleTv.text = "Exec. Once"
                holder.whenTv.text = mContext.getString(R.string.schedule_execute_once_row_text, time.hour, time.minute)
            }
            time != null -> {
                holder.titleTv.text = mContext.getString(R.string.schedule_time, time.hour, time.minute)
                holder.whenTv.text = mContext.getString(R.string.schedule_recurrent_row_text, time.hour, time.minute)
            }
            else -> {
                holder.titleTv.text = "On Boot"
                holder.whenTv.text = mContext.getString(R.string.schedule_boot_row_text)
            }
        }

        holder.profileTv.text = schedule.profile.scheduleName

        holder.optionsIb.setOnClickListener {

            with(PopupMenu(mContext, holder.optionsIb)) {
                menuInflater.inflate(R.menu.schedules_options_menu, this.menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.schedules_option_menu_delete -> {
                            mListener.onScheduleDeleteClick(mScheduleList[position])
                        }
                    }
                    true
                }

                show()
            }
        }
    }

    internal fun setList(profiles: List<Schedule>) {
        mScheduleList = profiles
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mScheduleList.size
    }

    fun getScheduleAt(pos: Int): Schedule {
        return mScheduleList[pos]
    }

    /**
     * Set the OnProfileClickListener, the parent must implement the interface.
     */
    fun setOnClickListener(profileClickListener: OnScheduleClickListener) {
        mListener = profileClickListener
    }
}