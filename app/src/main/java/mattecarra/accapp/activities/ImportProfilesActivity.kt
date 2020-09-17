package mattecarra.accapp.activities

import android.app.Activity
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_export.*
import kotlinx.android.synthetic.main.activity_import.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.ProfileEntriesAdapter
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileEntry
import mattecarra.accapp.utils.Constants
import java.io.Serializable

class ImportProfilesActivity : AppCompatActivity() {
    private lateinit var mAdapter: ProfileEntriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import)

        setSupportActionBar(import_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAdapter = ProfileEntriesAdapter()
        var importRecycler: RecyclerView = findViewById(R.id.import_profiles_rv)
        var layoutManager = LinearLayoutManager(this)

        importRecycler.layoutManager = layoutManager
        importRecycler.adapter = mAdapter
        importRecycler.isNestedScrollingEnabled = true

        var fab: ExtendedFloatingActionButton = findViewById(R.id.import_fab)
        fab.setOnClickListener { v -> importSelectedEntries() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.import_load_clipboard_menu -> loadClipboard()
            R.id.import_clear_menu -> clearEntries()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.import_menu, menu)
        return true
    }

    /*
    Load profiles from serialized text from clipboard
     */
    fun loadClipboard() {
        var clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData: String = ""

        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription!!.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            pasteData = clipboard.primaryClip?.getItemAt(0)?.text.toString()

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, ProfileEntry::class.java)
            // todo: put spinner while this works, put into coroutine, this can take a while for some reason (debugging overhead?)
            val jsonAdapter: JsonAdapter<List<ProfileEntry>> = moshi.adapter(listType)

            try {
                val result = jsonAdapter.fromJson(pasteData) as List<ProfileEntry>

                for (entry: ProfileEntry in result) {
                    mAdapter.addEntry(entry)
                }

                if (mAdapter.itemCount > 0) {
                    // Show recyclerview & hide label
                    import_profiles_rv.visibility = View.VISIBLE
                    import_profile_empty_tv.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.import_toast_no_valid_profile_json_clipboard), Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.import_toast_no_valid_profile_json_clipboard), Toast.LENGTH_LONG).show()
        }
    }

    fun clearEntries() {
        mAdapter.clearEntries()
        import_profiles_rv.visibility = View.GONE
        import_profile_empty_tv.visibility = View.VISIBLE
    }

    fun importSelectedEntries() {
        val checkedEntries = mAdapter.getCheckedEntries()
        if (checkedEntries.isNotEmpty()) {
            val returnIntent = Intent()
            returnIntent.putExtra(Constants.DATA_KEY, checkedEntries as Serializable)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.import_toast_no_selected_profiles), Toast.LENGTH_LONG).show()
        }

    }
 }