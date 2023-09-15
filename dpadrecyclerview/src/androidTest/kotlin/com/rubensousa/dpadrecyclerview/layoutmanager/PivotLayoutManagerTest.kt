package com.rubensousa.dpadrecyclerview.layoutmanager

import androidx.recyclerview.widget.RecyclerView.LayoutManager.Properties
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PivotLayoutManagerTest  {

    @Test
    fun testDefaultSpanCountIsSetThroughConstructor() {
        val properties = Properties()
        properties.spanCount = 5
        val pivotLayoutManager = PivotLayoutManager(properties)
        assertThat(pivotLayoutManager.getSpanCount()).isEqualTo(properties.spanCount)
    }

}
