package mattecarra.accapp.activities

import android.app.Fragment
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.beust.klaxon.Klaxon
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_import_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ImportExportFragmentPageAdapter
import mattecarra.accapp.fragments.ImportFragment
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem
import mattecarra.accapp.utils.ScopedFragment

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

        import_export_fab.setOnClickListener {
            when (mViewPager.currentItem) {
                0 -> {

                }
                1 -> {
                        val profiles: ArrayList<AccaProfile> = ArrayList()

                        for (export in pagerAdapter.getExportFragment().getExportList()) {
                            if (export.isChecked())
                                profiles.add(export.getProfile())
                        }

                        if (profiles.isNotEmpty()) {
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, Klaxon().toJsonString(profiles))
                                type = "text/plain"
                            }

                            val shareIntent = Intent.createChooser(sendIntent, null)
                            startActivity(shareIntent)
                        } else {
                            Toast.makeText(applicationContext, R.string.export_none_selected, Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        mViewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {

                // Change FAB text accordingly
                when (position) {
                    0 -> {
                        animateFab()
                        import_export_fab.text = getString(R.string.import_title)
                        import_export_fab.setIconResource(R.drawable.ic_outline_save_alt_24px)
                    }
                    1 -> {
                        animateFab()
                        import_export_fab.text = getString(R.string.export_title)
                        import_export_fab.setIconResource(R.drawable.ic_outline_publish_24px)
                    }
                }

                super.onPageSelected(position)
            }
        })


    }

    fun animateFab() {
        import_export_fab.hide()
        import_export_fab.show()
    }
}
