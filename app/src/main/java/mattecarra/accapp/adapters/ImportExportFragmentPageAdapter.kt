package mattecarra.accapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import mattecarra.accapp.fragments.ExportFragment
import mattecarra.accapp.fragments.ImportFragment
import kotlin.concurrent.fixedRateTimer

private const val NUM_ITEMS = 2

class ImportExportFragmentPageAdapter(fragment: FragmentActivity) : FragmentStateAdapter(fragment) {

    private lateinit var importFragment: ImportFragment
    private lateinit var exportFragment: ExportFragment

    override fun getItemCount(): Int {
        return NUM_ITEMS
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                importFragment = ImportFragment.newInstance()
                importFragment
            }
            1 -> {
                exportFragment = ExportFragment.newInstance()
                exportFragment
            }
            else -> {
                exportFragment = ExportFragment.newInstance()
                exportFragment
            }
        }
    }

    fun getImportFragment(): ImportFragment { return importFragment }
    fun getExportFragment(): ExportFragment { return exportFragment }
}