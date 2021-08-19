package mattecarra.accapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.adapters.ProfileListAdapter
import mattecarra.accapp.databinding.ProfilesFragmentBinding
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.ProfileUtils
import mattecarra.accapp.utils.ScopedFragment

// Fragments from: https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28

class ProfilesFragment : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener {
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
        val binding = ProfilesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = ProfilesFragmentBinding.bind(view)
        val profilesRecycler = binding.profileRecyclerView

        val context = requireContext()

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        mProfilesAdapter = ProfileListAdapter(context, ProfileUtils.getCurrentProfile(prefs))
        mProfilesAdapter.setOnClickListener(mListener)

        profilesRecycler.adapter = mProfilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(context)

        mViewModel = ViewModelProviders.of(this).get(ProfilesViewModel::class.java)

        // Observe data
        mViewModel.getProfiles().observe(viewLifecycleOwner, Observer { profiles ->
            if(profiles.isEmpty()) {
                binding.profilesEmptyTextview.visibility = View.VISIBLE
                profilesRecycler.visibility = View.GONE
            } else {
                binding.profilesEmptyTextview.visibility = View.GONE
                profilesRecycler.visibility = View.VISIBLE
            }
            mProfilesAdapter.setProfiles(profiles)
        })

        prefs.registerOnSharedPreferenceChangeListener(this)

        val itemTouchCallback = object: ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            private var swipeBack: Boolean = true
            private val background = ColorDrawable()
            private val backgroundColour = ContextCompat.getColor(context as Context, R.color.colorSuccessful)
            private val applyIcon = ContextCompat.getDrawable(context as Context, R.drawable.ic_outline_check_circle_24px)
            private val intrinsicWidth = applyIcon!!.intrinsicWidth
            private val intrinsicHeight = applyIcon!!.intrinsicHeight

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Required override, but not used
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // No up and down movement
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ACTION_STATE_SWIPE) {
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

                // Draw background
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                background.color = backgroundColour

                if (dX < 0) {

                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )

                    background.draw(c)

                    // Determine icon dimensions
                    val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val iconMargin = (itemHeight - intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + intrinsicWidth

                    // Draw the apply icon
                    val wrapped = DrawableCompat.wrap(applyIcon!!)
                    DrawableCompat.setTint(wrapped, Color.WHITE)
                    wrapped.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    wrapped.draw(c)
                }

                if (dX > 0) {

                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )

                    background.draw(c)

                    // Determine icon dimensions
                    val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val iconMargin = (itemHeight - intrinsicHeight) / 2
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + intrinsicWidth
                    val iconBottom = iconTop + intrinsicWidth

                    // Draw the apply icon
                    val wrapped = DrawableCompat.wrap(applyIcon!!)
                    DrawableCompat.setTint(wrapped, Color.WHITE)
                    wrapped.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    wrapped.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            @SuppressLint("ClickableViewAccessibility")
            private fun setTouchListener(canvas: Canvas, recyclerView: RecyclerView,
                                         viewHolder: RecyclerView.ViewHolder,
                                         dX: Float, dY: Float,
                                         actionState: Int, isCurrentlyActive: Boolean) {

                recyclerView.setOnTouchListener(object : View.OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                        when (event?.action) {
                            MotionEvent.ACTION_CANCEL -> swipeBack = true
                            MotionEvent.ACTION_UP -> swipeBack = true
                        }

                        if (swipeBack) {

                            if (dX > 300) { // If slid towards right > 300px?, adjust for sensitivity
                                 mListener.onProfileClick(mProfilesAdapter.getProfileAt(viewHolder.adapterPosition))
                            }
                            if (dX < -300) { // Show right side
                                 mListener.onProfileClick(mProfilesAdapter.getProfileAt(viewHolder.adapterPosition))
                            }
                        }

                        return false
                    }
                })
            }

            override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {

                if (swipeBack) {
                    swipeBack = false
                    return 0
                }
                return super.convertToAbsoluteDirection(flags, layoutDirection)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(profilesRecycler)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == Constants.PROFILE_KEY) {
            launch {
                val profileId = ProfileUtils.getCurrentProfile(sharedPreferences)

                val currentConfig = Acc.instance.readConfig()
                val selectedProfileConfig = mViewModel.getProfile(profileId)?.accConfig

                if(profileId != -1 && currentConfig != selectedProfileConfig) {
                    ProfileUtils.clearCurrentSelectedProfile(sharedPreferences) //if current profile and current config do not match -> the profile is no longer applied
                } else {
                    mProfilesAdapter.setActiveProfile(profileId)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnProfileClickListener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnProfileClickListener")
        }
    }
}
