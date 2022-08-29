package com.rubensousa.dpadrecyclerview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChildAlignmentTest {

    @Test
    fun `child is aligned to center by default`() {
        val childAlignment = ChildAlignment()
        assertThat(childAlignment.offsetRatio).isEqualTo(0.5f)
        assertThat(childAlignment.offset).isEqualTo(0)
        assertThat(childAlignment.isOffsetRatioEnabled).isTrue()
    }
}