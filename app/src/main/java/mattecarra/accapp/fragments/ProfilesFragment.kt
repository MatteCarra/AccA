package mattecarra.accapp.fragments

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.adapters.ProfileListAdapter

class ProfilesFragment : Fragment() {

    companion object {
        fun newInstance() = ProfilesFragment()
    }

    private lateinit var mViewModel: ProfilesViewModel
    private lateinit var mProfilesAdapter: ProfileListAdapter
    private lateinit var mListener: OnProfileClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profiles_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val profilesRecycler: RecyclerView = view.findViewById(R.id.profile_recyclerView)
        mProfilesAdapter = ProfileListAdapter(context!!)
        mProfilesAdapter.setOnClickListener(mListener)

        profilesRecycler.adapter = mProfilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(context)

        activity?.let {
            mViewModel = ViewModelProviders.of(it).get(ProfilesViewModel::class.java)

            // Observe data
            mViewModel.getProfiles().observe(this, Observer { profiles ->
                mProfilesAdapter.setProfiles(profiles)
            })
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnProfileClickListener) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnProfileClickListener")
        }
    }
}
