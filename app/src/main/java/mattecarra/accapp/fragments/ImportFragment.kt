package mattecarra.accapp.fragments

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.fragment_import.*
import mattecarra.accapp.R
import mattecarra.accapp.activities.ImportExportActivityViewModel
import mattecarra.accapp.adapters.ProfileExportAdapter
import mattecarra.accapp.adapters.ProfileImportAdapter
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem
import mattecarra.accapp.utils.ScopedFragment

class ImportFragment: ScopedFragment() {

    companion object {
        fun newInstance() = ImportFragment()
    }

    private val mViewModel: ImportExportActivityViewModel by activityViewModels()
    private lateinit var mProfilesRecycler: RecyclerView
    private lateinit var mProfileImportAdapter: ProfileImportAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Determine var/ls for RecyclerView
        mProfilesRecycler = import_profiles_rv
        mProfileImportAdapter = ProfileImportAdapter()
        mProfilesRecycler.adapter = mProfileImportAdapter
        mProfilesRecycler.layoutManager = LinearLayoutManager(context)

        mViewModel.getClipboardProfiles().observe(viewLifecycleOwner, Observer { profiles ->
            if (profiles.isNotEmpty()) {
                // Provide new data to adapter for recycler view
                mProfileImportAdapter.setProfiles(profiles)
                mProfilesRecycler.visibility = View.VISIBLE
                import_profile_empty_tv.visibility = View.GONE
            } else {
                mProfilesRecycler.visibility = View.GONE
                import_profile_empty_tv.visibility = View.VISIBLE
            }
        })

        import_load_clipboard_btn.setOnClickListener {
            // TODO: Access clipboard, try to parse AccaProfile objects
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            var ready = when {
                !clipboard.hasPrimaryClip() -> false
                !(clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))!! -> false
                else -> true
            }

            if (ready) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
//                Toast.makeText(context, "Received text: " + text, Toast.LENGTH_SHORT).show()
                // Process data
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                val listType = Types.newParameterizedType(List::class.java, AccaProfile::class.java)
                val jsonAdapter: JsonAdapter<List<AccaProfile>> = moshi.adapter(listType)
                val result = jsonAdapter.fromJson(text)

                if (result != null) {
                    // Provide new data as livedata to the viewmodel
                    mViewModel.setClipboardProfiles(result)
                } else {
                    Toast.makeText(context, "Invalid clipboard data.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Invalid clipboard data.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}