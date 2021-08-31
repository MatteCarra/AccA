package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import mattecarra.accapp.activities.MainActivity
import mattecarra.accapp.adapters.OnScheduleClickListener
import mattecarra.accapp.adapters.ScheduleProfileListAdapter
import mattecarra.accapp.databinding.SchedulesFragmentBinding
import mattecarra.accapp.models.Schedule
import mattecarra.accapp.utils.ScopedFragment

class SchedulesFragment : ScopedFragment(), OnScheduleClickListener {
    private lateinit var viewModel: SchedulesViewModel
    private lateinit var adapter: ScheduleProfileListAdapter
    private lateinit var binding : SchedulesFragmentBinding

    companion object {
        fun newInstance() = SchedulesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SchedulesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        activity?.let { activity ->
            viewModel = ViewModelProviders.of(activity).get(SchedulesViewModel::class.java)

            adapter = ScheduleProfileListAdapter(activity)
            adapter.setOnClickListener(this)
            binding.scheduleRecyclerView.adapter = adapter
            binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(context)

            viewModel.schedules.observe(viewLifecycleOwner, Observer { schedules ->
                if(schedules.isEmpty()) {
                    binding.schedulesEmptyTextview.visibility = View.VISIBLE
                    binding.scheduleRecyclerView.visibility = View.GONE
                } else {
                    binding.schedulesEmptyTextview.visibility = View.GONE
                    binding.scheduleRecyclerView.visibility = View.VISIBLE
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
