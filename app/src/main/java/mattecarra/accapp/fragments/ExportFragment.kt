package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ProfileExportAdapter
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
        })
    }

    // Provides the list of ProfileExportItems
    fun getExportList(): List<ProfileExportItem> {
        return mProfileExportAdapter.getExports()
    }
}