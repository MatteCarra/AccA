package mattecarra.accapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import mattecarra.accapp.fragments.ExportFragment
import mattecarra.accapp.fragments.ImportFragment

class ImportExportFragmentPageAdapter internal constructor(fm: FragmentManager, behaviour: Int) :
    FragmentPagerAdapter(fm, behaviour) {

    private val NUM_ITEMS = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> ExportFragment.newInstance()
            1 -> ImportFragment.newInstance()
            else -> ExportFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return NUM_ITEMS
    }
}