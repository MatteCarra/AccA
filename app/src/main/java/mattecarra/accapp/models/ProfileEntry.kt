package mattecarra.accapp.models

import java.io.Serializable

// Source: https://old.reddit.com/r/androiddev/comments/18yiwz/android_listview_adapter_with_checkbox_item/
// https://github.com/beemdevelopment/Aegis/blob/master/app/src/main/java/com/beemdevelopment/aegis/ui/models/ImportEntry.java

data class ProfileEntry(var profile: AccaProfile): Serializable {

    private val mName: String = profile.profileName
    private val mAccConfig: AccConfig = profile.accConfig

    @Transient private var mListener: Listener? = null
    private var mIsChecked: Boolean = false

    fun setOnCheckedChangedListener(listener: Listener?) {
        mListener = listener
    }

    fun isChecked(): Boolean {
        return mIsChecked
    }

    fun getName(): String {
        return mName
    }

    fun getConfig(): AccConfig {
        return mAccConfig
    }

    fun setIsChecked(isChecked: Boolean) {
        mIsChecked = isChecked

        mListener?.onCheckChanged(mIsChecked)
    }

    interface Listener {
        fun onCheckChanged(value: Boolean)
    }
}