package mattecarra.accapp.models

import android.view.View
import android.widget.CheckBox
import mattecarra.accapp.R

// Source: https://old.reddit.com/r/androiddev/comments/18yiwz/android_listview_adapter_with_checkbox_item/
// https://github.com/beemdevelopment/Aegis/blob/master/app/src/main/java/com/beemdevelopment/aegis/ui/models/ImportEntry.java

class ProfileExportItem(profile: AccaProfile, title: String) {

    private val mName: String = title
    private val mProfile: AccaProfile = profile
    private var mIsChecked: Boolean = false
    private var mListener: Listener? = null


    fun setOnCheckedChangedListener(listener: Listener?) {
        mListener = listener
    }

    fun getProfile(): AccaProfile {
        return mProfile
    }

    fun getName(): String {
        return mName
    }

    fun isChecked(): Boolean {
        return mIsChecked
    }

    // Toggle the internal variable for mIsChecked
    fun check() {
        mIsChecked = !mIsChecked
        mListener?.onCheckedChanged(mIsChecked)
    }

    interface Listener {
        fun onCheckedChanged(value: Boolean)
    }
}