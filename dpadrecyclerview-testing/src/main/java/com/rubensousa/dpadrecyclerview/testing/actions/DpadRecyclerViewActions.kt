/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.testing.actions

import android.graphics.Rect
import android.util.SparseArray
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.EspressoKey
import androidx.test.espresso.action.KeyEventAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.contrib.RecyclerViewActions.PositionableRecyclerViewAction
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.util.HumanReadables
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Useful [ViewAction]s for [DpadRecyclerView]. For other [ViewAction], check [DpadViewActions]
 */
object DpadRecyclerViewActions {

    private const val DEFAULT_KEY_PRESS_DELAY = 300

    /**
     * Similar to [RecyclerViewActions.scrollToHolder], but instead of invoking `scrollToPosition`,
     * it injects KeyEvents to simulate real DPAD events from the user
     */
    @JvmStatic
    fun <T : ViewHolder> scrollToHolder(
        viewHolderMatcher: Matcher<T>,
        keyPressDelay: Int = DEFAULT_KEY_PRESS_DELAY
    ): PositionableRecyclerViewAction {
        return ScrollToViewAction(viewHolderMatcher, keyPressDelay)
    }

    /**
     * Similar to [RecyclerViewActions.scrollToHolder], but instead of invoking `scrollToPosition`,
     * it injects KeyEvents to simulate real DPAD events from the user
     */
    @JvmStatic
    fun <T : ViewHolder> scrollTo(
        itemViewMatcher: Matcher<View>,
        keyPressDelay: Int = DEFAULT_KEY_PRESS_DELAY
    ): PositionableRecyclerViewAction {
        return scrollToHolder(ViewHolderMatcher<T>(itemViewMatcher), keyPressDelay)
    }

    @JvmStatic
    fun selectLastPosition(
        smooth: Boolean,
        onPositionSelected: (position: Int) -> Unit = {}
    ): ViewAction {
        return SelectLastPositionAction(smooth, onPositionSelected)
    }

    @JvmStatic
    fun selectPosition(
        position: Int,
        subPosition: Int = 0,
        smooth: Boolean = true,
    ): ViewAction {
        return SelectPositionAction(position, subPosition, smooth)
    }

    @JvmStatic
    fun selectSubPosition(
        subPosition: Int,
        smooth: Boolean = true,
    ): ViewAction {
        return SelectSubPositionAction(subPosition, smooth)
    }

    @JvmStatic
    fun getItemViewBounds(position: Int, rect: Rect): ViewAction {
        return executeForViewHolderItemViewAt(position, DpadViewActions.getViewBounds(rect))
    }

    @JvmStatic
    fun getRelativeItemViewBounds(position: Int, rect: Rect): ViewAction {
        return executeForViewHolderItemViewAt(
            position,
            DpadViewActions.getRelativeViewBounds(rect)
        )
    }

    @JvmStatic
    fun updateChildAlignment(alignment: ChildAlignment): ViewAction {
        return UpdateChildAlignmentAction(alignment)
    }

    @JvmStatic
    fun updateParentAlignment(alignment: ParentAlignment): ViewAction {
        return UpdateParentAlignmentAction(alignment)
    }

    @JvmStatic
    fun executeForViewHolderItemViewAt(position: Int, action: ViewAction): ViewAction {
        return ViewHolderItemViewAction(position, action)
    }

    @JvmStatic
    fun waitForAdapterUpdate(
        updates: Int = 1,
        failOnTimeout: Boolean = false,
        timeout: Long = 2,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForAdapterUpdateAction(updates, failOnTimeout, timeout, timeoutUnit)
    }

    @JvmStatic
    fun waitForIdleScroll(
        timeout: Long = 5,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForCondition<RecyclerView>(
            "Waiting for idle Scroll",
            { recyclerView -> recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE },
            timeout,
            timeoutUnit
        )
    }

    @JvmStatic
    fun execute(label: String, action: (recyclerView: DpadRecyclerView) -> Unit): ViewAction {
        return object : DpadRecyclerViewAction(label) {
            override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
                try {
                    action(recyclerView)
                } catch (exception: Exception) {
                    throw PerformException.Builder()
                        .withActionDescription(label)
                        .withCause(exception)
                        .withViewDescription(HumanReadables.describe(recyclerView))
                        .build()
                }
            }
        }
    }

    private fun <V : ViewHolder, T : V> itemsMatching(
        recyclerView: DpadRecyclerView,
        viewHolderMatcher: Matcher<V>,
        max: Int,
    ): List<MatchedItem> {
        @Suppress("UNCHECKED_CAST")
        val adapter: RecyclerView.Adapter<T> = requireNotNull(recyclerView.adapter)
                as RecyclerView.Adapter<T>
        val viewHolderCache: SparseArray<V> = SparseArray<V>()
        val matchedItems = ArrayList<MatchedItem>()
        for (position in 0 until adapter.itemCount) {
            val itemType = adapter.getItemViewType(position)
            var cachedViewHolder: V? = viewHolderCache[itemType]
            if (cachedViewHolder == null) {
                cachedViewHolder = adapter.createViewHolder(recyclerView, itemType)
                viewHolderCache.put(itemType, cachedViewHolder)
            }
            @Suppress("UNCHECKED_CAST")
            adapter.bindViewHolder(cachedViewHolder as T, position)
            if (viewHolderMatcher.matches(cachedViewHolder)) {
                matchedItems.add(
                    MatchedItem(
                        position,
                        HumanReadables.getViewHierarchyErrorMessage(
                            cachedViewHolder.itemView,
                            null,
                            "\n\n*** Matched ViewHolder item at position: $position ***",
                            null
                        )
                    )
                )
                adapter.onViewRecycled(cachedViewHolder)
                if (matchedItems.size == max) {
                    break
                }
            } else {
                adapter.onViewRecycled(cachedViewHolder)
            }
        }
        return matchedItems
    }

    private data class MatchedItem(val position: Int, val description: String) {
        override fun toString(): String = description
    }

    private class SelectPositionAction(
        private val position: Int,
        private val subPosition: Int,
        private val smooth: Boolean
    ) : DpadRecyclerViewAction("Selecting position $position with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            if (smooth) {
                if (subPosition > 0) {
                    recyclerView.setSelectedSubPositionSmooth(position, subPosition)
                } else {
                    recyclerView.setSelectedPositionSmooth(position)
                }
            } else {
                if (subPosition > 0) {
                    recyclerView.setSelectedSubPosition(position, subPosition)
                } else {
                    recyclerView.setSelectedPosition(position)
                }
            }
        }
    }

    private class SelectLastPositionAction(
        private val smooth: Boolean,
        private val onPositionSelected: (position: Int) -> Unit = {}
    ) : DpadRecyclerViewAction("Selecting last position") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            val itemCount = recyclerView.adapter?.itemCount ?: return
            val lastPosition = itemCount - 1
            if (smooth) {
                recyclerView.setSelectedPositionSmooth(lastPosition)
            } else {
                recyclerView.setSelectedPosition(lastPosition)
            }
            onPositionSelected(lastPosition)
        }
    }

    private class SelectSubPositionAction(
        private val subPosition: Int,
        private val smooth: Boolean
    ) : DpadRecyclerViewAction("Selecting subPosition $subPosition with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            if (smooth) {
                recyclerView.setSelectedSubPositionSmooth(subPosition)
            } else {
                recyclerView.setSelectedSubPosition(subPosition)
            }
        }

    }

    private class UpdateChildAlignmentAction(
        private val alignment: ChildAlignment,
        private val smooth: Boolean = false
    ) : DpadRecyclerViewAction("Updating child alignment to: $alignment with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            recyclerView.setChildAlignment(alignment, smooth)
        }

    }

    private class UpdateParentAlignmentAction(
        private val alignment: ParentAlignment,
        private val smooth: Boolean = false
    ) : DpadRecyclerViewAction("Updating parent alignment to: $alignment with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            recyclerView.setParentAlignment(alignment, smooth)
        }

    }

    private class ViewHolderItemViewAction(
        private val position: Int,
        private val action: ViewAction
    ) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return allOf(isAssignableFrom(RecyclerView::class.java))
        }

        override fun getDescription(): String {
            return "Performing ViewAction: $action.description on item at position $position"
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                ?: throw PerformException.Builder()
                    .withActionDescription(this.toString())
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(IllegalStateException("No view holder at position: $position"))
                    .build()
            action.perform(uiController, viewHolder.itemView)
        }

    }

    private class ViewHolderMatcher<V : ViewHolder>(
        val itemViewMatcher: Matcher<View>
    ) : TypeSafeMatcher<V>() {

        override fun matchesSafely(item: V): Boolean {
            return itemViewMatcher.matches(item.itemView)
        }

        override fun describeTo(description: Description) {
            description.appendText("holder with view: ")
            itemViewMatcher.describeTo(description)
        }
    }

    private class ScrollToViewAction<T : ViewHolder>(
        private val viewHolderMatcher: Matcher<T>,
        private val keyPressDelay: Int,
        private val atPosition: Int = RecyclerView.NO_POSITION
    ) : PositionableRecyclerViewAction {

        override fun atPosition(position: Int): PositionableRecyclerViewAction {
            check(position >= 0) { "$position is used as an index - must be >= 0" }
            return ScrollToViewAction(viewHolderMatcher, atPosition)
        }

        override fun getConstraints(): Matcher<View> {
            return allOf(isAssignableFrom(DpadRecyclerView::class.java), isDisplayed())
        }

        override fun getDescription(): String {
            return if (atPosition == RecyclerView.NO_POSITION) {
                "Scroll DpadRecyclerView to: $viewHolderMatcher"
            } else {
                "Scroll DpadRecyclerView to the ${atPosition}th matching $viewHolderMatcher"
            }
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerview = view as DpadRecyclerView
            try {
                val maxMatches = if (atPosition == RecyclerView.NO_POSITION) 2 else atPosition + 1
                val selectIndex = if (atPosition == RecyclerView.NO_POSITION) 0 else atPosition
                val matchedItems = itemsMatching(recyclerview, viewHolderMatcher, maxMatches)
                if (selectIndex >= matchedItems.size) {
                    throw RuntimeException(
                        String.format(
                            "Found %d items matching %s, but position %d was requested.",
                            matchedItems.size, viewHolderMatcher.toString(), atPosition
                        )
                    )
                }
                if (atPosition == RecyclerView.NO_POSITION && matchedItems.size > 1) {
                    val ambiguousViewError = StringBuilder()
                    ambiguousViewError.append(
                        "Found more than one sub-view matching $viewHolderMatcher"
                    )
                    matchedItems.forEach { item ->
                        ambiguousViewError.append("$item\n")
                    }
                    throw RuntimeException(ambiguousViewError.toString())
                }
                val matchPosition = matchedItems[selectIndex].position
                val currentPosition = recyclerview.getSelectedPosition()
                if (currentPosition == matchPosition) {
                    uiController.loopMainThreadUntilIdle()
                    return
                }
                if (recyclerview.getSpanCount() == 1) {
                    linearScrollToPosition(recyclerview, matchPosition, uiController, keyPressDelay)
                } else {
                    gridScrollToPosition(recyclerview, matchPosition, uiController, keyPressDelay)
                }
                uiController.loopMainThreadUntilIdle()
            } catch (e: RuntimeException) {
                throw PerformException.Builder()
                    .withActionDescription(this.description)
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(e)
                    .build()
            }
        }

    }

    private fun linearScrollToPosition(
        recyclerView: DpadRecyclerView,
        targetPosition: Int,
        uiController: UiController,
        keyPressDelay: Int
    ) {
        val currentPosition = recyclerView.getSelectedPosition()
        if (currentPosition == targetPosition) {
            return
        }
        val keyEventAction = getKeyEventActionForTarget(
            recyclerView, recyclerView.getOrientation(), currentPosition, targetPosition
        )
        val numberOfEvents = abs(targetPosition - currentPosition)
        repeat(numberOfEvents) {
            keyEventAction.perform(uiController, recyclerView)
            uiController.loopMainThreadForAtLeast(keyPressDelay.toLong())
        }
    }

    private fun getKeyEventActionForTarget(
        recyclerView: DpadRecyclerView,
        orientation: Int,
        currentPosition: Int,
        targetPosition: Int
    ): KeyEventAction {
        val keyCode = when (orientation) {
            RecyclerView.HORIZONTAL -> {
                if (currentPosition > targetPosition != recyclerView.isLayoutReversed()) {
                    KeyEvent.KEYCODE_DPAD_LEFT
                } else {
                    KeyEvent.KEYCODE_DPAD_RIGHT
                }
            }

            else -> {
                if (currentPosition > targetPosition != recyclerView.isLayoutReversed()) {
                    KeyEvent.KEYCODE_DPAD_UP
                } else {
                    KeyEvent.KEYCODE_DPAD_DOWN
                }
            }
        }
        return KeyEventAction(
            EspressoKey.Builder()
                .withKeyCode(keyCode)
                .build()
        )
    }

    private fun gridScrollToPosition(
        recyclerView: DpadRecyclerView,
        targetPosition: Int,
        uiController: UiController,
        keyPressDelay: Int
    ) {
        val spanSizeLookup = recyclerView.getSpanSizeLookup()
        var currentPosition = recyclerView.getSelectedPosition()
        val currentSpanGroup = spanSizeLookup.getSpanGroupIndex(
            currentPosition, recyclerView.getSpanCount()
        )
        val targetSpanGroup = spanSizeLookup.getSpanGroupIndex(
            targetPosition, recyclerView.getSpanCount()
        )
        val oppositeOrientation = if (recyclerView.getOrientation() == RecyclerView.VERTICAL) {
            RecyclerView.HORIZONTAL
        } else {
            RecyclerView.VERTICAL
        }
        if (currentSpanGroup == targetSpanGroup) {
            gridChangeFocusInSpanGroup(
                recyclerView,
                oppositeOrientation,
                currentPosition,
                targetPosition,
                uiController,
                keyPressDelay
            )
            return
        }
        val keyEventAction = getKeyEventActionForTarget(
            recyclerView, recyclerView.getOrientation(), currentPosition, targetPosition
        )
        val numberOfEvents = abs(targetSpanGroup - currentSpanGroup)
        repeat(numberOfEvents) {
            keyEventAction.perform(uiController, recyclerView)
            uiController.loopMainThreadForAtLeast(keyPressDelay.toLong())
        }
        currentPosition = recyclerView.getSelectedPosition()
        gridChangeFocusInSpanGroup(
            recyclerView,
            oppositeOrientation,
            currentPosition,
            targetPosition,
            uiController,
            keyPressDelay
        )
    }

    private fun gridChangeFocusInSpanGroup(
        recyclerView: DpadRecyclerView,
        oppositeOrientation: Int,
        currentPosition: Int,
        targetPosition: Int,
        uiController: UiController,
        keyPressDelay: Int
    ) {
        val keyEventAction = getKeyEventActionForTarget(
            recyclerView, oppositeOrientation, currentPosition, targetPosition
        )
        val numberOfEvents = abs(targetPosition - currentPosition)
        repeat(numberOfEvents) {
            keyEventAction.perform(uiController, recyclerView)
            uiController.loopMainThreadForAtLeast(keyPressDelay.toLong())
        }
    }

}
