package mattecarra.accapp.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.*
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.dialogs.*
import mattecarra.accapp.utils.Constants.ACC_VERSION
import mattecarra.accapp.djs.Djs
import mattecarra.accapp.utils.Constants.DJS_ENABLED
import mattecarra.accapp.utils.GithubUtils
import java.io.File
import kotlin.coroutines.CoroutineContext

class SettingsFragment : PreferenceFragmentCompat(), CoroutineScope {
    companion object {
	    fun newInstance() = SettingsFragment()
    }

    protected lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @SuppressLint("DefaultLocale")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)

        val telegram = findPreference<Preference>("acc_telegram")
        telegram?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            context?.let {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=acc_group")))
                } catch (ignored: Exception) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/acc_group")))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            true
        }

        val theme = findPreference<ListPreference>("theme")
        theme?.setOnPreferenceChangeListener { _, newValue ->
            when (newValue as String) {
                "0" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                "1" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                "2" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            true
        }

        val accVersion = findPreference<Preference>(ACC_VERSION)
        accVersion?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            context?.let { context ->
                val preferences = Preferences(context)

                MaterialDialog(context)
                    .show {
                        title(R.string.acc_version_picker_title)
                        message(R.string.acc_version_picker_message)
                        cancelOnTouchOutside(false)
                        launch {
                            accVersionSingleChoice(preferences.accVersion) { version ->
                                val installVersion: () -> Job = {
                                    this@SettingsFragment.launch {
                                        val dialog = MaterialDialog(context).show {
                                            title(R.string.installing_acc)
                                            progress(R.string.wait)
                                            cancelOnTouchOutside(false)
                                            onKeyCodeBackPressed { false }
                                        }

                                        val res = if (version == "bundled") {
                                            Acc.installBundledAccModule(context)
                                        } else {
                                            Acc.installAccModuleVersion(context, version)
                                        }

                                        dialog.dismiss()

                                        when(res?.code) {
                                            0 -> {
                                                if(version == "master" || version == "dev")
                                                    preferences.lastUpdateCheck = System.currentTimeMillis() / 1000
                                                preferences.lastCommit = GithubUtils.getLatestAccCommit(version)
                                                preferences.accVersion = version
                                            }
                                            else -> {
                                                MaterialDialog(context) //Dialog to tell the user that installation failed
                                                    .show {
                                                        title(R.string.acc_installation_failed_title)
                                                        message(R.string.installation_failed_non_bundled)
                                                        positiveButton(android.R.string.ok)
                                                        if(res != null)
                                                            shareLogsNeutralButton(File(context.filesDir, "logs/acc-install.log"), R.string.acc_installation_failed_log)
                                                    }
                                            }
                                        }
                                    }
                                }

                                if (version == "bundled") {
                                    installVersion()
                                } else {
                                    MaterialDialog(context)
                                        .show {
                                            title(R.string.acc_version_compatibility_warning_title)
                                            message(R.string.acc_version_compatibility_warning_description)
                                            positiveButton(android.R.string.yes) {
                                                installVersion()
                                            }
                                            negativeButton(android.R.string.cancel)
                                        }
                                }
                            }
                        }
                    }
            }

            true
        }

        val djsEnable = findPreference<CheckBoxPreference>(DJS_ENABLED)
        djsEnable?.setOnPreferenceChangeListener { _, isEnabled ->
            context?.let { context ->
                when {
                    isEnabled as Boolean && Djs.isDjsInstalled(context.filesDir) -> {
                        Djs.initDjs(context.filesDir)
                        true
                    }

                    isEnabled -> {
                        MaterialDialog(context).show {
                            title(R.string.installing_djs)
                            cancelOnTouchOutside(false)
                            onKeyCodeBackPressed { false }
                            djsInstallation(this@SettingsFragment, object: DjsInstallationListener {
                                override fun onInstallationFailed(result: Shell.Result?) {
                                    MaterialDialog(context) //Other installation errors can not be handled automatically -> show a dialog with the logs
                                        .show {
                                            title(R.string.djs_installation_failed_title)
                                            message(R.string.djs_installation_failed)
                                            positiveButton(android.R.string.ok)
                                            if(result != null)
                                                shareLogsNeutralButton(File(context.filesDir, "logs/djs-install.log"), R.string.djs_installation_failed_log)
                                        }
                                }

                                override fun onBusyboxMissing() {
                                    MaterialDialog(context)
                                        .show {
                                            title(R.string.installation_failed_busybox_title)
                                            message(R.string.installation_failed_busybox)
                                            positiveButton(android.R.string.ok)
                                            cancelOnTouchOutside(false)
                                        }
                                }

                                override fun onSuccess() {
                                    djsEnable.isChecked = true
                                }

                            })
                        }

                        false
                    }

                    else -> {
                        launch {
                            Djs.uninstallDjs(context.filesDir)
                        }

                        true
                    }
                }
            } ?: false
        }


        if(Acc.getAccVersion() >= 202002290) {
            findPreference<Preference>("current_measure_unit")?.isEnabled = false
            findPreference<Preference>("voltage_measure_unit")?.isEnabled = false
        }
    }
}