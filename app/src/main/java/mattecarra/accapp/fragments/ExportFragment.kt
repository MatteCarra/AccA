package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_export.*
import mattecarra.accapp.R
import mattecarra.accapp.activities.ImportExportActivityViewModel
import mattecarra.accapp.adapters.ProfileExportAdapter
import mattecarra.accapp.models.ProfileExportItem

class ExportFragment : Fragment() {

    companion object {
        fun newInstance() = ExportFragment()
    }

    private val mViewModel: ImportExportActivityViewModel by activityViewModels()
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

        // Observe list of ProfileExportItems
        mViewModel.getProfileExportItems().observe(viewLifecycleOwner, Observer { exportItems ->
            if (exportItems.isNotEmpty()) {
                mProfileExportAdapter.setProfiles(exportItems)
            }
        })
    }

    // Provides the list of ProfileExportItems
    fun getExportList(): List<ProfileExportItem> {
        return mProfileExportAdapter.getExports()
    }
}