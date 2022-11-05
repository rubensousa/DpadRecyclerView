package com.rubensousa.dpadrecyclerview.testing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class DpadSubPositionFragment : Fragment(R.layout.dpadrecyclerview_test_container) {

    private val adapter = DpadTestAdapter(showSubPositions = true)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.requestFocus()
        recyclerView.adapter = adapter
    }

}
