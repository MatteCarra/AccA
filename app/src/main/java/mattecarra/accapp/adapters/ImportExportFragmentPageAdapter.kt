package mattecarra.accapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import mattecarra.accapp.fragments.ExportFragment
import mattecarra.accapp.fragments.ImportFragment

class ImportExportFragmentPageAdapter internal constructor(
    fm: FragmentManager,
    lifeCycle: Lifecycle
) :
    FragmentStateAdapter(fm, lifeCycle) {

    private val NUM_ITEMS = 2

    override fun getItemCount(): Int {
        return NUM_ITEMS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExportFragment.newInstance()
            1 -> ImportFragment.newInstance()
            else -> ExportFragment.newInstance()
        }
    }
}