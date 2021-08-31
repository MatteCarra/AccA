package mattecarra.accapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ProfileEntriesAdapter
import mattecarra.accapp.databinding.ActivityExportBinding
import mattecarra.accapp.models.ProfileEntry

class ExportProfilesActivity: AppCompatActivity() {
    private lateinit var mAdapter: ProfileEntriesAdapter
    private lateinit var mEntries: List<ProfileEntry>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.exportToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAdapter = ProfileEntriesAdapter()
        var exportRecycler: RecyclerView = findViewById(R.id.export_entries_rv)
        var layoutManager = LinearLayoutManager(this)

        // Read from intent and deserialise
        mEntries = intent.getSerializableExtra("list") as ArrayList<ProfileEntry>

        exportRecycler.layoutManager = layoutManager
        exportRecycler.adapter = mAdapter
        exportRecycler.isNestedScrollingEnabled = true

        for (entry: ProfileEntry in mEntries) {
            mAdapter.addEntry(entry)
        }

        var fab: ExtendedFloatingActionButton = findViewById(R.id.export_fab)
        fab.setOnClickListener { v -> returnSelectedEntries() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun returnSelectedEntries() {
        val checkedEntries = mAdapter.getCheckedEntries()

        if (checkedEntries.isNotEmpty()) {
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listType =
                Types.newParameterizedType(List::class.java, ProfileEntry::class.java)
            val jsonAdapter: JsonAdapter<List<ProfileEntry>> = moshi.adapter(listType)
            val result = jsonAdapter.toJson(checkedEntries)

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, result)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        } else {
            Toast.makeText(
                applicationContext,
                R.string.export_none_selected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}