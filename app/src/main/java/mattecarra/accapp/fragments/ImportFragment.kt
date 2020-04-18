package mattecarra.accapp.fragments

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.beust.klaxon.Klaxon
import kotlinx.android.synthetic.main.fragment_import.*
import mattecarra.accapp.R
import mattecarra.accapp.models.AccaProfile
import mattecarra.accapp.models.ProfileExportItem
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        import_load_clipboard_btn.setOnClickListener {
            // TODO: Access clipboard, try to parse AccaProfile objects
            val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            var ready = when {
                !clipboard.hasPrimaryClip() -> false
                !(clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))!! -> false
                else -> true
            }

            if (ready) {
                val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
//                Toast.makeText(context, "Received text: " + text, Toast.LENGTH_SHORT).show()
                // Process data
                val result = Klaxon().parse<ArrayList<AccaProfile>>(text)
                if (result != null) {
                    Toast.makeText(context, "Number of profiles: " + result[0].profileName, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid clipboard data.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Invalid clipboard data.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}