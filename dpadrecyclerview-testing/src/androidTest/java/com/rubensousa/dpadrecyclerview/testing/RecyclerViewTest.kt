package com.rubensousa.dpadrecyclerview.testing

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertThat

abstract class RecyclerViewTest {

    private lateinit var subPositionFragment: FragmentScenario<DpadSubPositionFragment>
    private lateinit var gridFragment: FragmentScenario<DpadGridFragment>

    protected fun onGridFragment(block: (fragment: DpadGridFragment) -> Unit) {
        gridFragment.onFragment { fragment ->
            block(fragment)
        }
    }

    protected fun assertGridAdapterCount(size: Int) {
        var itemCount = 0
        onGridFragment { fragment ->
            itemCount = fragment.getAdapterSize()
        }
        assertThat(itemCount).isEqualTo(size)
    }

    protected fun assert(vararg assertions: ViewAssertion) {
        val interaction = getViewInteraction()
        assertions.forEach { assertion ->
            interaction.check(assertion)
        }
    }

    protected fun performActions(vararg actions: ViewAction) {
        getViewInteraction().perform(*actions)
    }

    private fun getViewInteraction(): ViewInteraction {
        return Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
    }

    protected fun launchGridFragment(): FragmentScenario<DpadGridFragment> {
        return launchFragmentInContainer<DpadGridFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            gridFragment = it
        }
    }

    protected fun launchSubPositionFragment(): FragmentScenario<DpadSubPositionFragment> {
        return launchFragmentInContainer<DpadSubPositionFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            subPositionFragment = it
        }
    }

}
