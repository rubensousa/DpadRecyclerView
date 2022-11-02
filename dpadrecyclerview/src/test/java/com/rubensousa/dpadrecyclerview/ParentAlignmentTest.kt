package com.rubensousa.dpadrecyclerview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ParentAlignmentTest {

    @Test
    fun `align to parent center by default`() {
        val parentAlignment = ParentAlignment()
        assertThat(parentAlignment.edge).isEqualTo(ParentAlignment.Edge.MIN_MAX)
        assertThat(parentAlignment.offsetRatio).isEqualTo(0.5f)
        assertThat(parentAlignment.offset).isEqualTo(0)
        assertThat(parentAlignment.isOffsetRatioEnabled).isTrue()
    }

}