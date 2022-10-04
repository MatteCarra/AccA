package mattecarra.accapp.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_SWIPE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.input.input
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mattecarra.accapp.R
import mattecarra.accapp._interface.OnScriptClickListener
import mattecarra.accapp.adapters.ScriptListAdapter
import mattecarra.accapp.databinding.*
import mattecarra.accapp.models.AccaScript
import mattecarra.accapp.utils.LogExt
import mattecarra.accapp.utils.ScopedFragment
import mattecarra.accapp.viewmodel.ScriptsViewModel

class ScriptesFragment : ScopedFragment(), OnScriptClickListener
{
    companion object
    {
        fun newInstance() = ScriptesFragment()
    }

    lateinit var mContext: Context
    private lateinit var mScriptsViewModel: ScriptsViewModel
    private lateinit var mScriptesAdapter: ScriptListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        return ScriptsFragmentBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        LogExt().d(javaClass.simpleName, "onViewCreated()")

        val binding = ScriptsFragmentBinding.bind(view)

        mContext = requireContext()

        mScriptesAdapter = ScriptListAdapter(mContext)
        mScriptesAdapter.setOnClickListener(this)

        binding.scriptsRecyclerView.adapter = mScriptesAdapter
        binding.scriptsRecyclerView.layoutManager = LinearLayoutManager(mContext)

        mScriptsViewModel = ViewModelProviders.of(this).get(ScriptsViewModel::class.java)

        // Observe data
        mScriptsViewModel.getLiveData().observe(viewLifecycleOwner, Observer { scripts ->

            if (scripts.isEmpty())
            {
                binding.scriptsEmptyTextview.visibility = View.VISIBLE
                binding.scriptsRecyclerView.visibility = View.GONE
            }
            else
            {
                binding.scriptsEmptyTextview.visibility = View.GONE
                binding.scriptsRecyclerView.visibility = View.VISIBLE
            }
            mScriptesAdapter.setScripts(scripts)
        })

        view.findViewById<FloatingActionButton>(R.id.scripts_addBtn_fab).setOnClickListener{ onAddScript() }

        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
        {

            private var swipeBack: Boolean = true
            private val background = ColorDrawable()
            private val backgroundColour = ContextCompat.getColor(context as Context, R.color.colorTransparent)
            private val applyIcon = ContextCompat.getDrawable(context as Context, R.drawable.ic_outline_check_circle_24px)
            private val intrinsicWidth = applyIcon!!.intrinsicWidth
            private val intrinsicHeight = applyIcon!!.intrinsicHeight

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
            {
                // Required override, but not used
            }

            override fun onMove(
                recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean
            {
                return false // No up and down movement
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            )
            {
                if (actionState == ACTION_STATE_SWIPE)
                {
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }

                // Draw background
                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                background.color = backgroundColour

                if (dX < 0)
                {
                    background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)

                    // Determine icon dimensions
                    val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val iconMargin = (itemHeight - intrinsicHeight) / 2
                    val iconLeft = itemView.right - iconMargin - intrinsicWidth
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + intrinsicWidth

                    // Draw the apply icon
                    val wrapped = DrawableCompat.wrap(applyIcon!!)
                    DrawableCompat.setTint(wrapped, Color.WHITE)
                    wrapped.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    wrapped.draw(c)
                }

                if (dX > 0)
                {

                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)

                    // Determine icon dimensions
                    val iconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val iconMargin = (itemHeight - intrinsicHeight) / 2
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + intrinsicWidth
                    val iconBottom = iconTop + intrinsicWidth

                    // Draw the apply icon
                    val wrapped = DrawableCompat.wrap(applyIcon!!)
                    DrawableCompat.setTint(wrapped, Color.WHITE)
                    wrapped.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                    wrapped.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            @SuppressLint("ClickableViewAccessibility")
            private fun setTouchListener(
                canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            )
            {

                recyclerView.setOnTouchListener(object : View.OnTouchListener
                {
                    override fun onTouch(v: View?, event: MotionEvent?): Boolean
                    {
                        when (event?.action)
                        {
                            MotionEvent.ACTION_CANCEL -> swipeBack = true
                            MotionEvent.ACTION_UP -> swipeBack = true
                        }

                        if (swipeBack)
                        {
                            if (dX > 300)
                            { // If slid towards right > 300px?, adjust for sensitivity
                                onScriptClick(mScriptesAdapter.getScriptAt(viewHolder.adapterPosition))
                            }
                            if (dX < -300)
                            { // Show right side
                                onScriptRunSilent(mScriptesAdapter.getScriptAt(viewHolder.adapterPosition))
                            }
                        }

                        return false
                    }
                })
            }

            override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int
            {
                if (swipeBack) { swipeBack = false ; return 0 }
                return super.convertToAbsoluteDirection(flags, layoutDirection)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.scriptsRecyclerView)
    }

    //-----------------------------------------------------------------------------------

    suspend fun runScript(script: AccaScript): AccaScript = withContext(Dispatchers.IO)
    {
        val sr = Shell.su(script.scBody).exec()
        script.scExitCode = sr.code
        script.scOutput = sr.out.joinToString(separator = "\n")
        script
    }

    override fun onScriptClick(script: AccaScript)
    {
        MaterialDialog(mContext).show {
            noAutoDismiss()
            title(text = script.scName)
            negativeButton { dismiss() }

            val binding = MdRunScriptBinding.inflate(layoutInflater)
            customView(view = binding.root, scrollable = true)
            binding.mdRunContent.setText(script.scBody)
            binding.mdStatusPb.visibility = View.VISIBLE

            launch {
                val sr = runScript(script)
                mScriptsViewModel.updateScript(script)

                if (isShowing) {

                    if (sr.scExitCode.equals(0)) {
                        binding.mdStatusImageView.setImageResource(R.drawable.ic_outline_check_circle_24px)
                        binding.mdStatusPb.visibility = View.INVISIBLE
                    } else {
                        binding.mdStatusImageView.setImageResource(R.drawable.ic_outline_error_outline_24px)
                        binding.mdStatusPb.visibility = View.INVISIBLE
                    }

                    binding.mdOutContent.setText(script.scOutput)
                    binding.mdOutContent.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onScriptRunSilent(script: AccaScript) {
        launch {
            Toast.makeText(mContext, "Running..\n" + script.scName, Toast.LENGTH_SHORT).show()
            val sr = runScript(script)
            mScriptsViewModel.updateScript(script)
            Toast.makeText(mContext, "Finished with result " + sr.scExitCode.equals(0).toString().uppercase(), Toast.LENGTH_SHORT).show()
        }
    }

    fun onAddScript()
    {
        MaterialDialog(mContext).show {

            noAutoDismiss()
            title(text = getString(R.string.new_script))
            val binding = ScriptNeweditDialogBinding.inflate(layoutInflater)
            customView(view = binding.root)
            var script = AccaScript(0,"","","","",0)

            positiveButton { dialog ->

                if (binding.scriptNameEd.text.trim().isEmpty()) {
                    binding.scriptNameEd.requestFocus() ; return@positiveButton }

                script.scName = binding.scriptNameEd.text.toString()
                script.scDescription = binding.scriptDescriptionEd.text.toString()
                script.scBody = binding.scriptBodyTextEd.text.toString()

                mScriptsViewModel.copyScript(script)
                dismiss()
            }

            negativeButton { dismiss() }
        }
    }

    override fun onEditScript(script: AccaScript)
    {
        MaterialDialog(mContext).show {

            noAutoDismiss()
            title(text = script.scName)
            val binding = ScriptNeweditDialogBinding.inflate(layoutInflater)
            customView(view = binding.root)
            binding.scriptNameEd.setText(script.scName)
            binding.scriptDescriptionEd.setText(script.scDescription)
            binding.scriptBodyTextEd.setText(script.scBody)

            positiveButton { dialog ->

                if (binding.scriptNameEd.text.trim().isEmpty()) {
                    binding.scriptNameEd.requestFocus() ; return@positiveButton }

                script.scName = binding.scriptNameEd.text.toString()
                script.scDescription = binding.scriptDescriptionEd.text.toString()
                script.scBody = binding.scriptBodyTextEd.text.toString()

                mScriptsViewModel.updateScript(script)
                dismiss()
            }

            negativeButton { dismiss() }
        }
    }

    override fun onRenameScript(script: AccaScript)
    {
        // Rename the selected script
        MaterialDialog(mContext).show {
            title(R.string.script_name)
            message(R.string.dialog_script_name_message)
            input(prefill = script.scName) { _, text ->
                script.scName = text.toString()
                mScriptsViewModel.updateScript(script)
            }
            positiveButton(R.string.save)
            negativeButton(android.R.string.cancel)
        }
    }

    override fun onCopyScript(script: AccaScript)
    {
        MaterialDialog(mContext).show {

            noAutoDismiss()
            title(text = getString(R.string.menu_option_copy))
            val binding = ScriptNeweditDialogBinding.inflate(layoutInflater)
            customView(view = binding.root)
            binding.scriptNameEd.setText(script.scName)
            binding.scriptDescriptionEd.setText(script.scDescription)
            binding.scriptBodyTextEd.setText(script.scBody)

            positiveButton { dialog ->

                if (binding.scriptNameEd.text.trim().isEmpty()) {
                    binding.scriptNameEd.requestFocus() ; return@positiveButton }

                script.scName = binding.scriptNameEd.text.toString()
                script.scDescription = binding.scriptDescriptionEd.text.toString()
                script.scBody = binding.scriptBodyTextEd.text.toString()

                mScriptsViewModel.copyScript(script)
                dismiss()
            }

            negativeButton { dismiss() }
        }
    }

    override fun onDeleteScript(script: AccaScript)
    {
        mScriptsViewModel.deleteScript(script)
        Toast.makeText(mContext, "deleteScript:\n"+script.scName, Toast.LENGTH_SHORT).show()
    }

}
