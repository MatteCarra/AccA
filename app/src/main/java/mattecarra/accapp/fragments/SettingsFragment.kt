package mattecarra.accapp.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.SingleChoiceListener
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import kotlinx.coroutines.*
import mattecarra.accapp.Preferences
import mattecarra.accapp.R
import mattecarra.accapp.acc.Acc
import mattecarra.accapp.utils.Constants.ACC_VERSION
import mattecarra.accapp.utils.progress
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

        val accVersion = findPreference<Preference>(ACC_VERSION)
        accVersion?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            context?.let { context ->
                val preferences = Preferences(context)

                val onSelection: SingleChoiceListener =  { _, _, text ->
                    val installVersion: () -> Unit = {
                        launch {
                            val dialog = MaterialDialog(context).show {
                                title(R.string.installing_acc)
                                progress(R.string.wait)
                                cancelOnTouchOutside(false)
                            }

                            dialog.setOnKeyListener { _, keyCode, _ ->
                                keyCode == KeyEvent.KEYCODE_BACK
                            }

                            if (text == "bundled")
                                Acc.installBundledAccModule(context)
                            else
                                Acc.installAccModuleVersion(context, text)

                            preferences.accVersion = text
                            dialog.dismiss()
                        }
                    }

                    if (text == "bundled") {
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

                val options = context.resources.getStringArray(R.array.acc_version_options).toMutableList()
                val versionDialog = MaterialDialog(context)
                    .show {
                        title(R.string.acc_version_preference_title)
                        message(R.string.acc_version_picker_message)
                        cancelOnTouchOutside(false)
                    }


                launch {
                    options.addAll(
                        Acc.listAccVersions()
                    )
                    versionDialog.listItemsSingleChoice(items = options, initialSelection = options.indexOf(preferences.accVersion), selection = onSelection)
                }
            }

            true
        }
    }
}