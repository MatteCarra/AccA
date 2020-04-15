package mattecarra.accapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.R
import mattecarra.accapp.adapters.LogRecyclerViewAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.File
import java.util.*

class LogViewerActivity : AppCompatActivity() {
    private val LOG_TAG = "LogViewerActivity"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LogRecyclerViewAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var onBottom = true
    private lateinit var job: Shell.Job
    private var isPaused = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun scrollToBottom() {
        recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private val clickerListener: (String) -> Unit = { line: String ->
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("log line", line))
        Toast.makeText(this, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.adapter.saveState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)

        val toolbar = findViewById<View>(R.id.log_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        supportActionBar?.title = getString(R.string.title_activity_log_view_file_name)

        recyclerView = findViewById<View>(R.id.log_recycler) as RecyclerView
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        adapter = LogRecyclerViewAdapter(ArrayList(), clickerListener)
        if (savedInstanceState != null) {
            this.adapter.restoreState(savedInstanceState)
        }
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
        linearLayoutManager.stackFromEnd = true

        this.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy == 0) {
                    return
                }
                if (dy < 0) {
                    onBottom = false
                } else if (!onBottom) {
                    if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                        onBottom = true
                    }
                }
            }
        })

        job = Shell.su("acc -L")
            .to(object : CallbackList<String>() {
                override fun onAddElement(e: String?) {
                    e?.let {
                        adapter.add(it, !isPaused)
                        if(!isPaused)
                            scrollToBottom()
                    }
                }

            })

        job.submit {
                println(it.code)
            }
    }

    override fun onDestroy() {
        Shell.getCachedShell()?.close()
        super.onDestroy()
    }

    override fun onResume() {
        Log.d(LOG_TAG, "onResume")
        adapter.notifyDataSetChanged()
        scrollToBottom()
        isPaused = false
        super.onResume()
    }

    override fun onPause() {
        Log.d(LOG_TAG, "onPause")
        isPaused = true
        super.onPause()
    }
}
