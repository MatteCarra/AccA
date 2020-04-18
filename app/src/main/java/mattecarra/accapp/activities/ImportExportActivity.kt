package mattecarra.accapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_import_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ImportExportFragmentPageAdapter

class ImportExportActivity : FragmentActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, ImportExportActivity::class.java))
        }
    }

    private lateinit var mViewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_export)

        mViewPager = findViewById(R.id.import_export_viewpager)

        val pagerAdapter = ImportExportFragmentPageAdapter(this)
        mViewPager.adapter = pagerAdapter

        TabLayoutMediator(
            import_export_tabs,
            import_export_viewpager,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                // Styling the tabs
                val tabLayout = import_export_tabs

                val itemCount = pagerAdapter.itemCount
                for (i in 0..itemCount) {
                    val tab: TabLayout.Tab = tabLayout.newTab()
                }

                when (position) {
                    0 -> tab.text = getString(R.string.import_title)
                    1 -> tab.text = getString(R.string.export_title)
                }
            }
        ).attach()
    }
}
