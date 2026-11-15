package com.example.threadhandlerwizard

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ProgressFragment : Fragment(R.layout.frag_progress) {
    private val vm: WorkViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bar = view.findViewById<ProgressBar>(R.id.progressBar)
        val text = view.findViewById<TextView>(R.id.txt)
        val txtPercent = view.findViewById<TextView>(R.id.txtPercent)

        bar.max = 100

        vm.status.observe(viewLifecycleOwner) { s ->
            text.text = s
            if (s == "Idle" || s.startsWith("準備中")) {
                bar.progress = 0
                txtPercent.text = "0%"
            }
        }

        vm.progress.observe(viewLifecycleOwner) { p ->
            bar.progress = p
            txtPercent.text = "%d%%".format(p)
        }
    }
}
