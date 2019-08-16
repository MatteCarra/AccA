package mattecarra.accapp.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TimePicker
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.schedules_fragment.*
import mattecarra.accapp.R
import mattecarra.accapp.activities.AccConfigEditorActivity
import mattecarra.accapp.adapters.ScheduleActionListener
import mattecarra.accapp.adapters.ScheduleRecyclerViewAdapter
import mattecarra.accapp.models.Schedule
import mattecarra.accapp.utils.ScopedFragment

class SchedulesFragment : ScopedFragment(), ScheduleActionListener {
    private lateinit var viewModel: SchedulesViewModel
    private lateinit var adapter: ScheduleRecyclerViewAdapter

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
        viewModel = ViewModelProviders.of(this).get(SchedulesViewModel::class.java)

        adapter = ScheduleRecyclerViewAdapter(this)
        schedule_recyclerView.adapter = adapter
        schedule_recyclerView.layoutManager = LinearLayoutManager(context)

        activity?.let { activity ->
            viewModel.schedules.observe(activity, Observer { schedules ->
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

    override fun onScheduleClick(schedule: Schedule) {
        context?.let {
            MaterialDialog(it).show {
                title(R.string.schedule_job)
                message(R.string.edit_scheduled_command)
                input(prefill = schedule.command, inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS, allowEmpty = false) { _, newCommand ->
                    viewModel.removeSchedule(schedule)
                    viewModel.addSchedule(schedule.copy(command = newCommand.toString()))
                }
                positiveButton(R.string.save)
                negativeButton(android.R.string.cancel)
                neutralButton(R.string.delete) {
                    viewModel.removeSchedule(schedule)
                }
            }
        }
    }

    override fun onScheduleDelete(schedule: Schedule) {
        viewModel.removeSchedule(schedule)
    }
}
