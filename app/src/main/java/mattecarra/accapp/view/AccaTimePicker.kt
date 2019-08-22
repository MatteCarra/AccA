package mattecarra.accapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TimePicker

class AccaTimePicker(context: Context?, attrs: AttributeSet?) : TimePicker(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Stop ScrollView from getting involved once you interact with the View
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            val p = parent
            p?.requestDisallowInterceptTouchEvent(true)
        }
        return false
    }
}