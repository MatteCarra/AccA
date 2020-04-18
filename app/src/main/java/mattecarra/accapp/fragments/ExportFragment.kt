package mattecarra.accapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ListView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.fragment_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ProfileExportAdapter
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem
import mattecarra.accapp.utils.ScopedFragment

class ExportFragment : ScopedFragment() {

    companion object {
        fun newInstance() = ExportFragment()
    }

    private lateinit var mViewModel: ExportViewModel
    private lateinit var mProfilesRecycler: RecyclerView
    private lateinit var mProfileExportAdapter: ProfileExportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mProfilesRecycler = export_list_rv
        mProfileExportAdapter = ProfileExportAdapter()
        mProfilesRecycler.adapter = mProfileExportAdapter
        mProfilesRecycler.layoutManager = LinearLayoutManager(context)

        mViewModel = ViewModelProvider(this).get(ExportViewModel::class.java)

        // Load list of profiles
        mViewModel.getProfiles().observe(viewLifecycleOwner, Observer { profiles ->
            //TODO: Create a nice 'no profiles' view to show/hide
            if (profiles.isEmpty()) {
                // Hide the view
            } else {
                // Show the view

                // Set data for recycler adapter
                mProfileExportAdapter.setProfiles(profiles)
            }

            // may have to move this out as it doesn't use livedata


        })

        // Export selected profiles as JSON string
        export_frag_fab.setOnClickListener {
            val profiles: ArrayList<AccaProfile> = ArrayList()

            for (export in mProfileExportAdapter.getExports()) {
                if (export.isChecked())
                    profiles.add(export.getProfile())
            }

            // TODO: Export to a file in the internal storage, or let the user select whether they want to share it, or save to file

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, Klaxon().toJsonString(profiles))
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

    }
}