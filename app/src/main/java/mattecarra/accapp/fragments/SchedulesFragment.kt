package mattecarra.accapp.fragments

import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.coroutines.launch

import mattecarra.accapp.R
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.utils.progress
import java.io.File

class SchedulesFragment : ScopedFragment() {

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
    }

    private fun checkDjsInstalled(): Boolean {
        context?.let { mContext ->
        }
        return true
    }
}
