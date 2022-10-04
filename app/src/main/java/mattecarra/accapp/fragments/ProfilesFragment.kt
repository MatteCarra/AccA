package mattecarra.accapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnProfileClickListener
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.activities.AccConfigEditorActivity
import mattecarra.accapp.adapters.ProfileListAdapter
import mattecarra.accapp.databinding.ProfilesFragmentBinding
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.LogExt
import mattecarra.accapp.utils.ProfileUtils
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.viewmodel.ProfilesViewModel
import mattecarra.accapp.viewmodel.SharedViewModel
import xml.BatteryInfoWidget
import xml.WIDGET_ALL_UPDATE

// Fragments from: https://codeburst.io/android-swipe-menu-with-recyclerview-8f28a235ff28

class ProfilesFragment : ScopedFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    OnProfileClickListener
{
    companion object
    {
        fun newInstance() = ProfilesFragment()
    }

    private lateinit var mProfilesViewModel: ProfilesViewModel
    private lateinit var mSharedViewModel: SharedViewModel
    private lateinit var mProfilesAdapter: ProfileListAdapter
    private lateinit var mContext: Context

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 7 && resultCode == Activity.RESULT_OK && data?.getBooleanExtra(Constants.ACC_HAS_CHANGES, false) == true)
        {
            launch {

                val uid = data.getIntExtra(Constants.PROFILE_ID_KEY, -1) as Int
                val newConfig = data.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig
                val newProfile = data.getSerializableExtra(Constants.PROFILE_CONFIG_KEY) as AccaProfile

                mProfilesViewModel.updateProfile(newProfile)
                Toast.makeText(mContext, mContext.getString(R.string.profile_tile_label, newProfile.profileName) + '\n' + mContext.getString(R.string.update_completed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return ProfilesFragmentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        LogExt().d(javaClass.simpleName, "onViewCreated()")

        val binding = ProfilesFragmentBinding.bind(view)
        val profilesRecycler = binding.profileRecyclerView

        mContext = requireContext()

        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
        mProfilesAdapter = ProfileListAdapter(mContext, ProfileUtils.getCurrentProfile(prefs))
        mProfilesAdapter.setOnClickListener(this)

        profilesRecycler.adapter = mProfilesAdapter
        profilesRecycler.layoutManager = LinearLayoutManager(mContext)

        mProfilesViewModel = ViewModelProviders.of(this).get(ProfilesViewModel::class.java)

        // Observe data
        mProfilesViewModel.getLiveData().observe(viewLifecycleOwner, Observer { profiles ->
            if (profiles.isEmpty()) {
                binding.profilesEmptyTextview.visibility = View.VISIBLE
                profilesRecycler.visibility = View.GONE
            } else {
                binding.profilesEmptyTextview.visibility = View.GONE
                profilesRecycler.visibility = View.VISIBLE
            }
            mProfilesAdapter.setProfiles(profiles)
        })

        prefs.registerOnSharedPreferenceChangeListener(this)

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            private var swipeBack: Boolean = true
            private val background = ColorDrawable()
            private val backgroundColour = ContextCompat.getColor(mContext, R.color.colorTransparent)
            private val applyIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_outline_check_circle_24px
            )
            private val intrinsicWidth = applyIcon!!.intrinsicWidth
            private val intrinsicHeight = applyIcon!!.intrinsicHeight

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
            {}

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean
            {
                return false // No up and down movement
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean)
            {
                if (actionState == ACTION_STATE_SWIPE)
                {
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

                // Draw background
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                background.color = backgroundColour

                if (dX < 0) {

                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
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

                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
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
            private fun setTouchListener(
                canvas: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {

                recyclerView.setOnTouchListener(object : View.OnTouchListener {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                        when (event?.action) {
                            MotionEvent.ACTION_CANCEL -> swipeBack = true
                            MotionEvent.ACTION_UP -> swipeBack = true
                        }

                        if (swipeBack) {

                            if (dX > 300) { // If slid towards right > 300px?, adjust for sensitivity
                                onProfileClick(mProfilesAdapter.getProfileAt(viewHolder.adapterPosition))
                            }
                            if (dX < -300) { // Show right side
                                onProfileClick(mProfilesAdapter.getProfileAt(viewHolder.adapterPosition))
                            }
                        }

                        return false
                    }
                })
            }

            override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int
            {
                if (swipeBack) { swipeBack = false ; return 0 }
                return super.convertToAbsoluteDirection(flags, layoutDirection)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(profilesRecycler)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String)
    {
        if (key == Constants.PROFILE_KEY)
        {
            launch {
                val profileId = ProfileUtils.getCurrentProfile(sharedPreferences)
                val currentConfig = Acc.instance.readConfig()
                val selectedProfileConfig = mProfilesViewModel.getProfileById(profileId)?.accConfig

                if (profileId != -1 && currentConfig != selectedProfileConfig)
                    ProfileUtils.clearCurrentSelectedProfile(sharedPreferences)
                else mProfilesAdapter.setActiveProfile(profileId)
            }
        }
    }

    /**
     * Override function for handling ProfileOnClicks
     * Applies the selected profile as CURRENT!
     */
    override fun onProfileClick(profile: AccaProfile)
    {
        LogExt().d(javaClass.simpleName, "onProfileClick(${profile.uid}): "+ profile.profileName)

        launch {
            mSharedViewModel.setCurrentSelectedProfile(profile.uid)
            mSharedViewModel.updateAccConfig(profile.accConfig)
            mContext.sendBroadcast(Intent(mContext, BatteryInfoWidget::class.java)
                .setAction(WIDGET_ALL_UPDATE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        // Display Toast for the user.
        Toast.makeText(mContext, getString(R.string.selecting_profile_toast, profile.profileName), Toast.LENGTH_LONG).show()
    }

    override fun onProfileLongClick(profile: AccaProfile)
    {
    }

    override fun editProfile(profile: AccaProfile)
    {
        // Edit the configuration of the selected profile.
        startActivityForResult(Intent(mContext, AccConfigEditorActivity::class.java)
            .putExtra(Constants.PROFILE_ID_KEY, profile.uid)
            .putExtra(Constants.PROFILE_CONFIG_KEY, profile)
            .putExtra(Constants.TITLE_KEY, profile.profileName), 7)
    }

    override fun renameProfile(profile: AccaProfile)
    {
        // Rename the selected profile (2nd option).
        MaterialDialog(mContext).show {
                title(R.string.profile_name)
                message(R.string.dialog_profile_name_message)
                input(prefill = profile.profileName) { _, charSequence ->
                    // Set profile name
                    profile.profileName = charSequence.toString()
                    // Update the profile in the DB
                    mProfilesViewModel.updateProfile(profile)
                }
                positiveButton(R.string.save)
                negativeButton(android.R.string.cancel)
            }
    }

    override fun deleteProfile(profile: AccaProfile)
    {
        // Delete the selected profile (3rd option).
        mProfilesViewModel.deleteProfile(profile)
    }
}
