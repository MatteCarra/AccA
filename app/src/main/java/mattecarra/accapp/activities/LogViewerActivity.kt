package mattecarra.accapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import kotlinx.android.synthetic.main.activity_log_viewer.*
import mattecarra.accapp.R
import mattecarra.accapp.adapters.LogRecyclerViewAdapter
import java.util.*

class LogViewerActivity : AppCompatActivity()
{
    private val LOG_TAG = "LogViewerActivity"

    private lateinit var adapter: LogRecyclerViewAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var onBottom = true
    private lateinit var job: Shell.Job
    private var isPaused = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId)
        {
            android.R.id.home ->
            {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun scrollToBottom() {
        log_recycler.scrollToPosition(adapter.itemCount - 1)
        onBottom = true
        log_button_scroll_end.visibility = View.GONE
    }

    private fun setTitleCount(count: Int)
    {
        supportActionBar?.title = getString(R.string.title_activity_log_view_file_name) + ": " + count.toString()
    }

    private val clickerListener: (String) -> Unit = { line: String ->
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("log line", line))
        Toast.makeText(this, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT).show()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        this.adapter.saveState(outState)
        outState.putBoolean("paused", isPaused)
        outState.putBoolean("onBottom", onBottom)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_viewer)

        setSupportActionBar(log_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.title_activity_log_view_file_name)

        linearLayoutManager = LinearLayoutManager(this)
        log_recycler.layoutManager = linearLayoutManager
        adapter = LogRecyclerViewAdapter(ArrayList(), clickerListener)
        if (savedInstanceState != null){
            isPaused = savedInstanceState.getBoolean("paused")
            onBottom = savedInstanceState.getBoolean("onBottom")
            this.adapter.restoreState(savedInstanceState)
        }
        log_recycler.adapter = adapter
        log_recycler.setHasFixedSize(true)
        linearLayoutManager.stackFromEnd = true

        log_button_scroll_end.setOnClickListener { scrollToBottom() }
        log_button_clear.setOnClickListener { adapter.clearAll(); setTitleCount(0) }

        log_button_pause.setImageResource(if (isPaused) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24)
        log_button_pause.setOnClickListener {
            isPaused = !isPaused
            log_button_pause.setImageResource(if (isPaused) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24)
        }

        log_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                if (dy == 0) return
                if (dy < 0) {
                    onBottom = false
                    log_button_scroll_end.visibility = View.VISIBLE
                } else if (!onBottom && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    onBottom = true
                    log_button_scroll_end.visibility = View.GONE
                }
            }
        })

        job = Shell.su("acc -L").to(object : CallbackList<String>()
            {
                override fun onAddElement(e: String?)
                {
                    e?.let {
                        adapter.add(it, !isPaused)
                        if (!isPaused && onBottom) scrollToBottom()
                        setTitleCount(adapter.lines.size)
                    }
                }

            })

        job.submit { println(it.code) }
    }

    override fun onDestroy() {
        Shell.getCachedShell()?.close()
        super.onDestroy()
    }
}
