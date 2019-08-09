package mattecarra.accapp.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomnavsheet_fragment.*
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnNavigationItemClicked

class BottomNavSheetFragment: BottomSheetDialogFragment() {

    private lateinit var mListener: OnNavigationItemClicked

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottomnavsheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appbar_nav.setNavigationItemSelectedListener {
            // Send to main activity to load in new fragment
            mListener.handleClick(it)
            true
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnNavigationItemClicked) {
            mListener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnNavigationItemClicked")
        }
    }
}