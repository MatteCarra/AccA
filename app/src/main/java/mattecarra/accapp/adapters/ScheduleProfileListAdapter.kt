package mattecarra.accapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import mattecarra.accapp.R
import mattecarra.accapp.databinding.ScheduleItemBinding
import mattecarra.accapp.models.Schedule

interface OnScheduleClickListener {
    fun onScheduleProfileClick(schedule: Schedule)
    fun onScheduleDeleteClick(schedule: Schedule)
    fun onScheduleToggle(schedule: Schedule, isEnabled: Boolean)
}

class ScheduleProfileListAdapter internal constructor(context: Context) :
    RecyclerView.Adapter<ScheduleProfileListAdapter.ProfileViewHolder>() {
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mScheduleList = emptyList<Schedule>()
    private lateinit var mListener: OnScheduleClickListener
    private val mContext = context

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ScheduleItemBinding.bind(itemView)

        val titleTv = binding.itemScheduleTitleTv
        val profileTv = binding.itemScheduleProfileTv
        val whenTv = binding.itemScheduleWhenTv
        val optionsIb = binding.itemScheduleOptionsIb
        val scheduleToogleSwitch = binding.itemScheduleToggleSwitch
        val profileCapacityTv = binding.itemProfileCapacityTv
        val profileTemperatureTv = binding.itemProfileTemperatureTv
        val profileOnPlugTv = binding.itemProfileOnPlugTv


        init {
            itemView.setOnClickListener { v ->
                if (binding.scheduleDetailsLl.visibility == View.GONE) {
                    binding.scheduleDetailsLl.visibility = View.VISIBLE
                    titleTv.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_arrow_drop_up_24px,
                        0
                    )
                } else {
                    binding.scheduleDetailsLl.visibility = View.GONE
                    titleTv.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_baseline_arrow_drop_down_24px,
                        0
                    )
                }
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ScheduleItemBinding.inflate(mInflater,parent,false)
        val itemView = binding.root
        return ProfileViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val schedule = mScheduleList[position]
        val time = schedule.getTime()

        when {
            time != null && schedule.executeOnce -> {
                holder.titleTv.text = mContext.getString(R.string.schedule_execute_once_row_title, time.hour, time.minute)
                holder.whenTv.text = mContext.getString(R.string.schedule_execute_once_row_text, time.hour, time.minute)
            }
            time != null -> {
                holder.titleTv.text = mContext.getString(R.string.schedule_time, time.hour, time.minute)
                holder.whenTv.text = mContext.getString(R.string.schedule_recurrent_row_text, time.hour, time.minute)
            }
            else -> {
                holder.titleTv.text = mContext.getString(R.string.execute_on_boot_schedule)
                holder.whenTv.text = mContext.getString(R.string.schedule_boot_row_text)
            }
        }

        val config = schedule.profile.accConfig
        holder.profileCapacityTv.text = config.configCapacity.toString(mContext)
        holder.profileTemperatureTv.text = config.configTemperature.toString(mContext)
        holder.profileOnPlugTv.text = config.getOnPlug(mContext)

        holder.scheduleToogleSwitch.isChecked = schedule.isEnabled
        holder.scheduleToogleSwitch.setOnCheckedChangeListener { _, isChecked ->
            mListener.onScheduleToggle(mScheduleList[position], isChecked)
        }

        holder.profileTv.text = schedule.profile.scheduleName

        holder.optionsIb.setOnClickListener {
            with(PopupMenu(mContext, holder.optionsIb)) {
                menuInflater.inflate(R.menu.schedules_options_menu, this.menu)

                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.schedules_option_menu_edit -> {
                            mListener.onScheduleProfileClick(mScheduleList[position])
                        }
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