package mattecarra.accapp.models

import android.view.View
import android.widget.CheckBox
import mattecarra.accapp.R

// Source: https://old.reddit.com/r/androiddev/comments/18yiwz/android_listview_adapter_with_checkbox_item/
class RowProfileExportData {

    private var mCheckState: Boolean = false
    private lateinit var mProfile: AccaProfile
    private lateinit var mTitle: String
    private val mListener: View.OnClickListener

    constructor(mProfile: AccaProfile, mTitle: String) {
        this.mProfile = mProfile
        this.mTitle = mTitle
    }


    init {
        mListener = View.OnClickListener { v ->
            val checkbox = v.findViewById<CheckBox>(R.id.pex_item_checkbox)
            if (checkbox.isInTouchMode) {
                setCheckState(!getCheckState())
                checkbox.isChecked = !getCheckState()
            }
        }
    }

    fun getListener(): View.OnClickListener {
        return this.mListener
    }

    fun setProfile(profile: AccaProfile) {
        this.mProfile = profile
    }

    fun getProfile(): AccaProfile {
        return this.mProfile
    }

    private fun setText(text: String) {
        this.mTitle = text
    }

    fun getText(): String {
        return this.mTitle
    }

    fun getOnClickListener(): View.OnClickListener {
        return this.mListener
    }

    fun getCheckState(): Boolean {
        return this.mCheckState
    }

    private fun setCheckState(value: Boolean) {
        this.mCheckState = value
    }
}