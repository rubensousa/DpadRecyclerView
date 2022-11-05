package com.rubensousa.dpadrecyclerview.testing

import android.graphics.Rect
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import org.junit.Test

class DpadViewActionsTest : RecyclerViewTest() {

    @Test
    fun testRetrievingViewBounds() {
        launchGridFragment()

        val expectedRect = Rect()
        performActions(DpadRecyclerViewActions.execute("Getting view bounds") { recyclerView ->
            recyclerView.getGlobalVisibleRect(expectedRect)
        })

        val actualRect = Rect()
        performActions(DpadViewActions.getViewBounds(actualRect))

        assertThat(actualRect).isEqualTo(expectedRect)
    }

    @Test
    fun testFocusChanges() {
        launchGridFragment()
        
        var hasFocus = true

        performActions(DpadViewActions.clearFocus())
        performActions(DpadRecyclerViewActions.execute("Checking focus state") { recyclerView ->
            hasFocus = recyclerView.hasFocus()
        })

        assertThat(hasFocus).isEqualTo(false)
        hasFocus = false

        performActions(DpadViewActions.requestFocus())
        performActions(DpadRecyclerViewActions.execute("Checking focus state") { recyclerView ->
            hasFocus = recyclerView.hasFocus()
        })

        assertThat(hasFocus).isEqualTo(true)
    }

}
