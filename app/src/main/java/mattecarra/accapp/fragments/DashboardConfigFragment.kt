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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.launch
import mattecarra.accapp.R
import mattecarra.accapp.viewmodel.SharedViewModel
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.activities.AccConfigEditorActivity
import mattecarra.accapp.databinding.ProfilesItemBinding
import mattecarra.accapp.models.AccConfig
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.utils.Constants
import mattecarra.accapp.utils.ProfileUtils
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.viewmodel.ProfilesViewModel

class DashboardConfigFragment() : ScopedFragment(), SharedPreferences.OnSharedPreferenceChangeListener  {
    private lateinit var mContext: Context
    private lateinit var mViewModel: ProfilesViewModel
    private lateinit var mSharedViewModel: SharedViewModel
    private lateinit var mPrefs: SharedPreferences

    private var mActiveProfile: Boolean = false

    private var _binding: ProfilesItemBinding? = null
    private val binding get() = _binding!!
    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data?.getBooleanExtra(Constants.ACC_HAS_CHANGES, false) == true) {
            launch {
                mSharedViewModel.updateAccConfig(data.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig) //TODO: Check assertion
                // Remove the current selected profile
                mSharedViewModel.clearCurrentSelectedProfile()

                updateInfo(AccaProfile(0, getString(R.string.profile_not_selected), data.getSerializableExtra(Constants.ACC_CONFIG_KEY) as AccConfig))
            }

        }
    }

    companion object
    {
        fun newInstance() = DashboardConfigFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = ProfilesItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.itemProfileLoadImage.visibility = View.VISIBLE;
        binding.itemProfileInfo.visibility = View.GONE;
        binding.editConfigButton.visibility = View.VISIBLE;

        mContext = requireContext()
        mViewModel = ViewModelProvider(this).get(ProfilesViewModel::class.java)
        mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        mPrefs.registerOnSharedPreferenceChangeListener(this)

        view.setOnClickListener(View.OnClickListener {
            startAccConfigEditorActivity()
        })

        binding.editConfigButton.setOnClickListener {
            startAccConfigEditorActivity()
        }

        checkProfile()
    }

    private fun startAccConfigEditorActivity() {
        activityLauncher.launch(Intent(context, AccConfigEditorActivity::class.java))
    }

    fun checkProfile() {
        launch {

            val profileId = ProfileUtils.getCurrentProfile(mPrefs)
            val currentConfig = Acc.instance.readConfig()
            val selectedProfileConfig = mViewModel.getProfileById(profileId)?.accConfig

            if (profileId == -1 || currentConfig != selectedProfileConfig)
            {
                updateInfo(AccaProfile(0, getString(R.string.profile_not_selected), currentConfig))
            }
            else
            {
                updateInfo(AccaProfile(0, mViewModel.getProfileById(profileId)?.profileName.toString(), currentConfig))
            }
        }
    }

    fun updateInfo(mProfile: AccaProfile) {
        binding.itemProfileTitleTextView.text = mProfile.profileName
        binding.itemProfileCapacityTv.text = mProfile.accConfig.configCapacity.toString(mContext)

        // if on\off getting from AccConfigEditorActivity :: 168
        // todo prop temperatureTv on\off no exist in table\data ... coolDownLL too
        binding.itemProfileTemperatureLl.visibility = if (mProfile.accConfig.configTemperature.coolDownTemperature >= 90 && mProfile.accConfig.configTemperature.maxTemperature >= 95) View.GONE else View.VISIBLE;
        binding.itemProfileTemperatureTv.text = mProfile.accConfig.configTemperature.toString(mContext)

        // todo add and integrate current
        binding.itemProfileChargingVoltageLl.visibility = if (mProfile.accConfig.configVoltage.controlFile == null && mProfile.accConfig.configVoltage.max == null) View.GONE else View.VISIBLE;
        binding.itemProfileChargingVoltageTv.text = mProfile.accConfig.configVoltage.toString()

        // if on\off getting from AccConfigEditorActivity :: 196
        binding.itemProfileCooldownLl.visibility = if (mProfile.accConfig.configCoolDown == null || mProfile.accConfig.configCoolDown!!.atPercent > 100) View.GONE else View.VISIBLE;
        binding.itemProfileCooldownTv.text = mProfile.accConfig.configCoolDown?.toString(mContext)

        binding.itemProfileOnBootLl.visibility = if (mProfile.accConfig.configOnBoot == null) View.GONE else View.VISIBLE
        binding.itemProfileOnBootTv.text = mProfile.accConfig.configOnBoot

        binding.itemProfileOnPlugLl.visibility = if (mProfile.accConfig.configOnPlug == null) View.GONE else View.VISIBLE
        binding.itemProfileOnPlugTv.text = mProfile.accConfig.getOnPlug(mContext);

        binding.itemProfileOptionsIb.visibility = View.GONE
        binding.itemProfileSelectedIndicatorView.visibility = if (mActiveProfile) View.VISIBLE else View.GONE

        binding.itemProfileLoadImage.visibility = View.GONE;
        binding.itemProfileInfo.visibility = View.VISIBLE;
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String)
    {
        if (key == Constants.PROFILE_KEY) checkProfile()
    }
}