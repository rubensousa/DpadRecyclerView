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

package com.rubensousa.dpadrecyclerview.layoutmanager

import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

/**
 * Helper for dealing with accessibility
 */
internal class LayoutAccessibilityHelper(
    private val layoutManager: LayoutManager,
    private val configuration: LayoutConfiguration,
    private val layoutInfo: LayoutInfo,
    private val pivotSelector: PivotSelector,
    private val scroller: LayoutScroller
) {

    fun getRowCountForAccessibility(state: RecyclerView.State): Int {
        if (configuration.isHorizontal()) {
            return configuration.spanCount
        }
        return if (state.itemCount < 1) {
            0
        } else {
            // Row count is one more than the last item's row index
            layoutInfo.getSpanGroupIndex(state.itemCount - 1) + 1
        }
    }

    fun getColumnCountForAccessibility(state: RecyclerView.State): Int {
        if (configuration.isVertical()) {
            return configuration.spanCount
        }
        return if (state.itemCount < 1) {
            0
        } else {
            // Column count is one more than the last item's column index.
            layoutInfo.getSpanGroupIndex(state.itemCount - 1) + 1
        }
    }

    fun onInitializeAccessibilityNodeInfo(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        info: AccessibilityNodeInfoCompat
    ) {
        val count = state.itemCount
        val reverseLayout = layoutInfo.shouldReverseLayout()

        // If focusOutFront/focusOutEnd is false, override Talkback in handling
        // backward/forward actions by adding such actions to supported action list.
        if (!configuration.focusOutFront || (count > 1 && !layoutInfo.isItemFullyVisible(0))) {
            addA11yActionMovingBackward(info, reverseLayout)
        }

        if (!configuration.focusOutBack
            || (count > 1 && !layoutInfo.isItemFullyVisible(count - 1))
        ) {
            addA11yActionMovingForward(info, reverseLayout)
        }

        info.setCollectionInfo(
            CollectionInfoCompat
                .obtain(
                    getRowCountForAccessibility(state),
                    getColumnCountForAccessibility(state),
                    layoutManager.isLayoutHierarchical(recycler, state),
                    layoutManager.getSelectionModeForAccessibility(recycler, state)
                )
        )
    }

    fun onInitializeAccessibilityNodeInfoForItem(
        host: View,
        info: AccessibilityNodeInfoCompat
    ) {
        val layoutParams: ViewGroup.LayoutParams = host.layoutParams
        if (layoutParams !is DpadLayoutParams) {
            return
        }
        val spanGroupIndex = layoutInfo.getSpanGroupIndex(layoutParams.viewLayoutPosition)
        if (layoutInfo.isHorizontal()) {
            info.setCollectionItemInfo(
                AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                    layoutParams.spanIndex, layoutParams.spanSize,
                    spanGroupIndex, 1, false, false
                )
            )
        } else {
            info.setCollectionItemInfo(
                AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                    spanGroupIndex, 1,
                    layoutParams.spanIndex, layoutParams.spanSize, false, false
                )
            )
        }
    }

    fun performAccessibilityAction(
        recyclerView: RecyclerView?,
        state: RecyclerView.State,
        action: Int
    ): Boolean {
        if (!configuration.isScrollEnabled) {
            // Consume request to avoid talkback focusing out of the RecyclerView
            return true
        }
        val translatedAction = translateAccessibilityAction(
            action, layoutInfo.shouldReverseLayout()
        )

        val scrollingReachedStart = (pivotSelector.position == 0
                && translatedAction == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)

        val scrollingReachedEnd = (pivotSelector.position == state.itemCount - 1
                && translatedAction == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)

        if (scrollingReachedStart || scrollingReachedEnd) {
            /**
             * Send a fake scroll completion event to notify Talkback
             * that the scroll event was handled.
             * This will happen when focusing out from DpadRecyclerView is not allowed.
             */
            sendViewScrolledAccessibilityEvent(recyclerView)
        } else {
            when (translatedAction) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                    scroller.addScrollMovement(forward = true, consume = true)
                }
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                    scroller.addScrollMovement(forward = false, consume = true)
                }
            }
        }
        return true
    }

    private fun translateAccessibilityAction(action: Int, reverseLayout: Boolean): Int {
        if (Build.VERSION.SDK_INT < 23) {
            return action
        }
        if (configuration.isHorizontal()) {
            if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT.id) {
                return if (reverseLayout) {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
                } else {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                }
            } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT.id) {
                return if (reverseLayout) {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                } else {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
                }
            }
        } else { // VERTICAL layout
            if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_UP.id) {
                return AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
            } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_DOWN.id) {
                return AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
            }
        }
        return action
    }

    private fun sendViewScrolledAccessibilityEvent(recyclerView: RecyclerView?) {
        val event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_VIEW_SCROLLED)
        recyclerView?.apply {
            onInitializeAccessibilityEvent(event)
            requestSendAccessibilityEvent(this, event)
        }
    }

    private fun addA11yActionMovingBackward(
        info: AccessibilityNodeInfoCompat,
        reverseLayout: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (configuration.isHorizontal()) {
                val action = if (reverseLayout) {
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT
                } else {
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT
                }
                info.addAction(action)
            } else {
                info.addAction(
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_UP
                )
            }
        } else {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
        }
        info.isScrollable = true
    }

    private fun addA11yActionMovingForward(
        info: AccessibilityNodeInfoCompat,
        reverseLayout: Boolean
    ) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (configuration.isHorizontal()) {
                val action = if (reverseLayout) {
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_LEFT
                } else {
                    AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT
                }
                info.addAction(action)
            } else {
                info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_DOWN)
            }
        } else {
            info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
        }
        info.isScrollable = true
    }

}
