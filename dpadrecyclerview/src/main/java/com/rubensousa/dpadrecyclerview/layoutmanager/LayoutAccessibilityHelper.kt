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
import com.rubensousa.dpadrecyclerview.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

/**
 * Helper for dealing with accessibility
 */
internal class LayoutAccessibilityHelper(
    private val layoutManager: LayoutManager,
    private val configuration: LayoutConfiguration,
    private val layoutInfo: LayoutInfo,
    private val selectionState: ViewHolderSelector,
    private val scroller: LayoutScroller
) {

    fun getRowCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (configuration.isHorizontal()) {
            return configuration.spanCount
        }
        return if (state.itemCount < 1) {
            0
        } else {
            // Row count is one more than the last item's row index
            layoutInfo.getSpanGroupIndex(recycler, state, state.itemCount - 1) + 1
        }
    }

    fun getColumnCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (configuration.isVertical()) {
            return configuration.spanCount
        }
        return if (state.itemCount < 1) {
            0
        } else {
            // Column count is one more than the last item's column index.
            layoutInfo.getSpanGroupIndex(recycler, state, state.itemCount - 1) + 1
        }
    }

    fun onInitializeAccessibilityNodeInfo(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        info: AccessibilityNodeInfoCompat
    ) {
        val count = state.itemCount
        val reverseLayout = configuration.reverseLayout

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
                    getRowCountForAccessibility(recycler, state),
                    getColumnCountForAccessibility(recycler, state),
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
        val position = layoutParams.absoluteAdapterPosition
        val rowIndex = if (position >= 0) {
            layoutInfo.getRowIndex(position)
        } else {
            -1
        }
        if (rowIndex < 0) {
            return
        }
        val guessSpanIndex: Int = position / configuration.spanCount
        if (configuration.isHorizontal()) {
            info.setCollectionItemInfo(
                AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                    rowIndex, 1, guessSpanIndex, 1, false, false
                )
            )
        } else {
            info.setCollectionItemInfo(
                AccessibilityNodeInfoCompat.CollectionItemInfoCompat.obtain(
                    guessSpanIndex, 1, rowIndex, 1, false, false
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
        val translatedAction = translateAccessibilityAction(action, configuration.reverseLayout)

        val scrollingReachedStart = (selectionState.position == 0
                && translatedAction == AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)

        val scrollingReachedEnd = (selectionState.position == state.itemCount - 1
                && translatedAction == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)

        if (scrollingReachedStart || scrollingReachedEnd) {
            // Send a fake scroll completion event to notify Talkback that the scroll event was
            // successful. Hence, Talkback will only look for next focus within the RecyclerView.
            // Not sending this will result in Talkback classifying it as a failed scroll event, and
            // will try to jump focus out of the RecyclerView.
            // We know at this point that either focusOutFront or focusOutEnd is true (or both),
            // because otherwise, we never hit ACTION_SCROLL_BACKWARD/FORWARD here.
            sendViewScrolledAccessibilityEvent(recyclerView)
        } else {
            when (translatedAction) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                    scroller.dispatchPendingMovement(false)
                    scroller.dispatchSelectionMoves(false, -1)
                }
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                    scroller.dispatchPendingMovement(true)
                    scroller.dispatchSelectionMoves(false, 1)
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
                return if(reverseLayout) {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD
                } else {
                    AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                }
            } else if (action == AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_RIGHT.id) {
                 return if (reverseLayout){
                     AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD
                 } else{
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
        recyclerView?.let { recyclerView ->
            recyclerView.onInitializeAccessibilityEvent(event)
            recyclerView.requestSendAccessibilityEvent(recyclerView, event)
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
