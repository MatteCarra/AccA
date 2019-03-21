package mattecarra.accapp.fragments

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
import mattecarra.accapp.adapters.ProfileListAdapter

class ProfilesFragment : Fragment() {

    companion object {
        fun newInstance() = ProfilesFragment()
    }

    private lateinit var viewModel: ProfilesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.profiles_fragment, container, false)

        val profilesRecycler = view.findViewById<RecyclerView>(R.id.profile_recyclerView)
        val profilesAdapter = ProfileListAdapter(this.context!!)

        profilesRecycler.adapter = profilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(this.context)

        // Observe data
        viewModel.getProfiles().observe(this, Observer { profiles ->
            profiles?.let { profilesAdapter.setProfiles(it) }
        })

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ProfilesViewModel::class.java)

    }

}
