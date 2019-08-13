package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ScheduleRecyclerViewAdapter
import mattecarra.accapp.utils.ScopedFragment

class SchedulesFragment : ScopedFragment() {
    private lateinit var viewModel: SchedulesViewModel
    private lateinit var adapter: ScheduleRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.schedules_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SchedulesViewModel::class.java)
        adapter = ScheduleRecyclerViewAdapter { schedule, repeat ->
            viewModel.addSchedule(schedule)
        }

        viewModel.schedules.observe(this, Observer {
            adapter.setList(it)
        })
    }

    private fun checkDjsInstalled(): Boolean {
        context?.let { mContext ->
        }
        return true
    }
}
