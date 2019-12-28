package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ProfileExportAdapter
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.RowProfileExportData
import mattecarra.accapp.utils.ScopedFragment

class ExportFragment : ScopedFragment(), CompoundButton.OnCheckedChangeListener {

    companion object {
        fun newInstance() = ExportFragment()
    }

    private lateinit var mViewModel: ExportViewModel
    private lateinit var mListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mListView = export_frag_profile_lv

        mViewModel = ViewModelProvider(this).get(ExportViewModel::class.java)

        // Load list of profiles
        mViewModel.getProfiles().observe(this, Observer { profiles ->
            //TODO: Create a nice 'no profiles' view to show/hide
            if (profiles.isEmpty()) {
                // Hide the view
            } else {
                // Show the view
            }

            mListView.adapter = ProfileExportAdapter(view.context, profiles, this)
        })

        export_frag_fab.setOnClickListener { view ->
            mViewModel.exportProfiles()
        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, checked: Boolean) {
        val pos = mListView.getPositionForView(buttonView)
        if (pos != ListView.INVALID_POSITION) {
            var exProfile: RowProfileExportData = mListView.adapter.getItem(pos) as RowProfileExportData
            if (checked) {
                mViewModel.addProfileToExport(exProfile.getProfile())
            } else {
                mViewModel.removeProfileToExport(exProfile.getProfile())
            }

        }
    }
}