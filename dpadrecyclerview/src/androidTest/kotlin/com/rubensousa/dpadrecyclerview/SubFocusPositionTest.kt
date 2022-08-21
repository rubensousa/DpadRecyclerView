package com.rubensousa.dpadrecyclerview

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.test.R

class SubFocusPositionTest : GridTest() {

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return TestGridFragment.LayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX,
                offset = 0,
                offsetPercent = 50f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetPercent = 50f
            )
        )
    }

    class TestFragment : Fragment(R.layout.test_container) {

        
    }

}