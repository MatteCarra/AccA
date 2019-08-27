package mattecarra.accapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mattecarra.accapp.R
import mattecarra.accapp.utils.ScopedFragment

class ExportFragment: ScopedFragment() {

    companion object {
        fun newInstance() = ExportFragment()
    }

    private lateinit var mViewModel: ExportViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_export, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}