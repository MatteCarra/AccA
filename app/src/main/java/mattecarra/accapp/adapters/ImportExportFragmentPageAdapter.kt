package mattecarra.accapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import mattecarra.accapp.fragments.ExportFragment
import mattecarra.accapp.fragments.ImportFragment

private const val NUM_ITEMS = 2

class ImportExportFragmentPageAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return NUM_ITEMS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ImportFragment.newInstance()
            1 -> ExportFragment.newInstance()
            else -> ExportFragment.newInstance()
        }
    }
}