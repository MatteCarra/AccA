package mattecarra.accapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topjohnwu.superuser.CallbackList
import com.topjohnwu.superuser.Shell
import mattecarra.accapp.R
import mattecarra.accapp.adapters.LogRecyclerViewAdapter
import mattecarra.accapp.databinding.ActivityLogViewerBinding
import java.util.*

class LogViewerActivity : AppCompatActivity()
{
    private val LOG_TAG = "LogViewerActivity"

    private lateinit var binding: ActivityLogViewerBinding
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
        binding.logRecycler.scrollToPosition(adapter.itemCount - 1)
        onBottom = true
        binding.logButtonScrollEnd.visibility = View.GONE
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
        binding = ActivityLogViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.logToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = getString(R.string.title_activity_log_view_file_name)

        linearLayoutManager = LinearLayoutManager(this)
        binding.logRecycler.layoutManager = linearLayoutManager
        adapter = LogRecyclerViewAdapter(ArrayList(), clickerListener)
        if (savedInstanceState != null){
            isPaused = savedInstanceState.getBoolean("paused")
            onBottom = savedInstanceState.getBoolean("onBottom")
            this.adapter.restoreState(savedInstanceState)
        }
        binding.logRecycler.adapter = adapter
        binding.logRecycler.setHasFixedSize(true)
        linearLayoutManager.stackFromEnd = true

        binding.logButtonScrollEnd.setOnClickListener { scrollToBottom() }
        binding.logButtonClear.setOnClickListener { adapter.clearAll(); setTitleCount(0) }

        binding.logButtonPause.setImageResource(if (isPaused) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24)
        binding.logButtonPause.setOnClickListener {
            isPaused = !isPaused
            binding.logButtonPause.setImageResource(if (isPaused) R.drawable.ic_baseline_play_arrow_24 else R.drawable.ic_baseline_pause_24)
        }

        binding.logRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener()
        {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
            {
                if (dy == 0) return
                if (dy < 0) {
                    onBottom = false
                    binding.logButtonScrollEnd.visibility = View.VISIBLE
                } else if (!onBottom && linearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.itemCount - 1) {
                    onBottom = true
                    binding.logButtonScrollEnd.visibility = View.GONE
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
