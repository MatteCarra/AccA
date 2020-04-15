package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ImportExportFragmentPageAdapter
import mattecarra.accapp.utils.ScopedFragment

class ImportFragment: ScopedFragment() {

    companion object {
        fun newInstance() = ImportFragment()
    }

    private lateinit var mViewModel: ImportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_import, container, false)
    }

}