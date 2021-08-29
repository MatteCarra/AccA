package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.schedules_fragment.*
import mattecarra.accapp.R
import mattecarra.accapp.activities.MainActivity
import mattecarra.accapp.adapters.OnScheduleClickListener
import mattecarra.accapp.adapters.ScheduleProfileListAdapter
import mattecarra.accapp.models.Schedule
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.viewmodel.SchedulesViewModel

class SchedulesFragment : ScopedFragment(), OnScheduleClickListener {
    private lateinit var viewModel: SchedulesViewModel
    private lateinit var adapter: ScheduleProfileListAdapter

    companion object {
        fun newInstance() = SchedulesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.schedules_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        activity?.let { activity ->
            viewModel = ViewModelProviders.of(activity).get(SchedulesViewModel::class.java)

            adapter = ScheduleProfileListAdapter(activity)
            adapter.setOnClickListener(this)
            schedule_recyclerView.adapter = adapter
            schedule_recyclerView.layoutManager = LinearLayoutManager(context)

            viewModel.schedules.observe(viewLifecycleOwner, Observer { schedules ->
                if(schedules.isEmpty()) {
                    schedules_empty_textview.visibility = View.VISIBLE
                    schedule_recyclerView.visibility = View.GONE
                } else {
                    schedules_empty_textview.visibility = View.GONE
                    schedule_recyclerView.visibility = View.VISIBLE
                }

                adapter.setList(schedules)
            })
        }
    }

    override fun onScheduleProfileClick(schedule: Schedule) {
        (activity as MainActivity?)?.editSchedule(schedule)
    }

    override fun onScheduleToggle(schedule: Schedule, isEnabled: Boolean) {
        viewModel.editSchedule(schedule.profile.uid, schedule.profile.scheduleName, isEnabled, schedule.time, schedule.executeOnce, schedule.executeOnBoot, schedule.profile.accConfig)
    }

    override fun onScheduleDeleteClick(schedule: Schedule) {
        viewModel.removeSchedule(schedule)
    }
}
