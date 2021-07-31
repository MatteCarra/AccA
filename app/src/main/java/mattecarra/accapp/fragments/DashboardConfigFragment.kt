package mattecarra.accapp.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.profiles_item.*
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.SharedViewModel
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.activities.AccConfigEditorActivity
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.ProfileUtils
import mattecarra.accapp.utils.ScopedFragment

class DashboardConfigFragment() : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener
{
    private lateinit var mContext: Context
    private lateinit var mViewModel: ProfilesViewModel
    private lateinit var mPrefs: SharedPreferences

    private var mActiveProfile: Boolean = false
    private var mClickListener: View.OnClickListener? = null
    private val ACC_CONFIG_EDITOR_REQUEST: Int = 1

    companion object
    {
        fun newInstance() = DashboardConfigFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return inflater.inflate(R.layout.profiles_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        item_profile_load_image.visibility = View.VISIBLE;
        item_profile_info.visibility = View.GONE;

        mContext = requireContext()
        mViewModel = ViewModelProviders.of(this).get(ProfilesViewModel::class.java)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mPrefs.registerOnSharedPreferenceChangeListener(this)

        view.setOnClickListener(  View.OnClickListener {
            Intent(view.context, AccConfigEditorActivity::class.java).also { intent ->
                startActivityForResult(intent, ACC_CONFIG_EDITOR_REQUEST) }
        })

        checkProfile()
    }

    fun checkProfile()
    {
        launch {

            val profileId = ProfileUtils.getCurrentProfile(mPrefs)
            val currentConfig = Acc.instance.readConfig()
            val selectedProfileConfig = mViewModel.getProfile(profileId)?.accConfig

            if(profileId == -1 || currentConfig != selectedProfileConfig)
            {
                updateInfo(AccaProfile(0, getString(R.string.profile_not_selected), currentConfig))
            }
            else
            {
                updateInfo(AccaProfile(0, mViewModel.getProfile(profileId)?.profileName.toString(), currentConfig))
            }
        }
    }

    fun updateInfo(mProfile: AccaProfile)
    {
        item_profile_title_textView.text = mProfile.profileName
        item_profile_capacity_tv.text = mProfile.accConfig.configCapacity.toString(mContext)

        // if on\off getting from AccConfigEditorActivity :: 168
        // todo prop temperatureTv on\off no exist in table\data ... coolDownLL too
        item_profile_temperature_ll.visibility = if(mProfile.accConfig.configTemperature.coolDownTemperature >= 90 && mProfile.accConfig.configTemperature.maxTemperature >= 95) View.GONE else View.VISIBLE;
        item_profile_temperature_tv.text = mProfile.accConfig.configTemperature.toString(mContext)

        // todo add and integrate current
        item_profile_charging_voltage_ll.visibility = if (mProfile.accConfig.configVoltage.controlFile == null && mProfile.accConfig.configVoltage.max == null) View.GONE else View.VISIBLE;
        item_profile_charging_voltage_tv.text = mProfile.accConfig.configVoltage.toString()

        // if on\off getting from AccConfigEditorActivity :: 196
        item_profile_cooldown_ll.visibility = if(mProfile.accConfig.configCoolDown == null || mProfile.accConfig.configCoolDown!!.atPercent > 100)  View.GONE else View.VISIBLE;
        item_profile_cooldown_tv.text = mProfile.accConfig.configCoolDown?.toString(mContext)

        item_profile_on_boot_ll.visibility = if (mProfile.accConfig.configOnBoot == null) View.GONE else View.VISIBLE
        item_profile_on_boot_tv.text = mProfile.accConfig.configOnBoot

        item_profile_on_plug_ll.visibility = if (mProfile.accConfig.configOnPlug == null) View.GONE else View.VISIBLE
        item_profile_on_plug_tv.text = mProfile.accConfig.getOnPlug(mContext);

        item_profile_options_ib.visibility = View.GONE
        item_profile_selectedIndicator_view.visibility = if (mActiveProfile) View.VISIBLE else View.GONE

        item_profile_load_image.visibility = View.GONE;
        item_profile_info.visibility = View.VISIBLE;
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String)
    {
        if (key == Constants.PROFILE_KEY) checkProfile()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode)
        {
            ACC_CONFIG_EDITOR_REQUEST ->
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    val _sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

                    if (data?.getBooleanExtra(Constants.ACC_HAS_CHANGES, false) == true)
                    {
                        launch {
                            _sharedViewModel.updateAccConfig(data.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig) //TODO: Check assertion

                            // Remove the current selected profile
                            _sharedViewModel.clearCurrentSelectedProfile()

                            updateInfo(AccaProfile(0, getString(R.string.profile_not_selected), data.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig))
                        }
                    }
                }
            }
        }
    }

}