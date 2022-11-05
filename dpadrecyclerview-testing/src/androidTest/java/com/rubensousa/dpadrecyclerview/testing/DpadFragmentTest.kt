package com.rubensousa.dpadrecyclerview.testing

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertThat
import org.junit.Before

abstract class DpadFragmentTest {

    private lateinit var fragmentScenario: FragmentScenario<DpadTestFragment>

    @Before
    fun setup() {
        fragmentScenario = launchFragmentInContainer(
            themeResId = R.style.DpadRecyclerViewTestTheme
        )
    }

    protected fun onFragment(block: (fragment: DpadTestFragment) -> Unit) {
        fragmentScenario.onFragment { fragment ->
            block(fragment)
        }
    }

    protected fun getSelectionEvents(): List<DpadSelectionEvent> {
        var events = listOf<DpadSelectionEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectionEvents()
        }
        return events
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

    protected fun assertAdapterCount(size: Int) {
        var itemCount = 0
        onFragment { fragment ->
            itemCount = fragment.getAdapterSize()
        }
        assertThat(itemCount).isEqualTo(size)
    }

    private fun getViewInteraction(): ViewInteraction {
        return Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
    }

}