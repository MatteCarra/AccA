package mattecarra.accapp.fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import mattecarra.accapp.R

class SchedulesFragment : Fragment() {

    companion object {
        fun newInstance() = SchedulesFragment()
    }

    private lateinit var viewModel: SchedulesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.schedules_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SchedulesViewModel::class.java)
        // TODO: Use the ViewModel
    }
}
