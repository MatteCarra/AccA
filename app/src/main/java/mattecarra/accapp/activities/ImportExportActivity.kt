package mattecarra.accapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_import_export.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ImportExportFragmentPageAdapter

class ImportExportActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, ImportExportActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_export)

        

        val adapter = ImportExportFragmentPageAdapter(supportFragmentManager, lifecycle)

        import_export_viewpager.adapter = adapter

        TabLayoutMediator(
            import_export_tabs,
            import_export_viewpager,
            TabLayoutMediator.OnConfigureTabCallback { tab, position ->
                // Styling
//                val tabLayout = import_export_tabs
//
//                if (adapter != null) {
//                    val itemCount = adapter.itemCount
//                    for (i in 0..itemCount) {
//                        val tab: TabLayout.Tab = tabLayout.newTab()
//
//                    }
//                }
                tab.text = "Tab $position"
            }).attach()
    }
}
