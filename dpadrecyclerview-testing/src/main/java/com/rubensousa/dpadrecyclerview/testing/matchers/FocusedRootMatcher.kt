package com.rubensousa.dpadrecyclerview.testing.matchers

import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class FocusedRootMatcher : TypeSafeMatcher<Root>() {

    override fun describeTo(description: Description) {
        description.appendText("has focus")
    }

    public override fun matchesSafely(root: Root): Boolean {
        return root.decorView.hasFocus()
    }

}
