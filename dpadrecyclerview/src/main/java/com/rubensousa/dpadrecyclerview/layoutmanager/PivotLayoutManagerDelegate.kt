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

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment

/**
 * Acts as a migration bridge from [DpadLayoutManager] to [PivotLayoutManager]
 */
interface PivotLayoutManagerDelegate {

    fun setRecyclerView(recyclerView: RecyclerView?)

    fun setChildrenDrawingOrderEnabled(enabled: Boolean)

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener)

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener)

    fun clearOnViewHolderSelectedListeners()

    fun setGravity(gravity: Int)

    fun setOrientation(orientation: Int)

    fun setSpanCount(spanCount: Int)

    fun getSpanCount(): Int

    fun onRtlPropertiesChanged()

    fun selectPosition(position: Int, subPosition: Int, smooth: Boolean)

    fun selectSubPosition(subPosition: Int, smooth: Boolean)

    fun getSelectedPosition(): Int

    fun getSelectedSubPosition(): Int

    fun getCurrentSubPositions(): Int

    fun setFocusableDirection(direction: FocusableDirection)

    fun getFocusableDirection(): FocusableDirection

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean)

    fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean)

    fun setSmoothFocusChangesEnabled(isEnabled: Boolean)

    fun setFocusSearchDisabled(disabled: Boolean)

    fun isFocusSearchDisabled(): Boolean

    fun onInterceptFocusSearch(focused: View, direction: Int): View?

    fun onFocusChanged(gainFocus: Boolean)

    fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean?

    fun setAlignments(
        parent: ParentAlignment,
        child: ChildAlignment,
        smooth: Boolean
    )

    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean)

    fun getParentAlignment(): ParentAlignment

    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean)

    fun getChildAlignment(): ChildAlignment

    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup)

    fun addOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener)

    fun removeOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener)

    fun clearOnLayoutCompletedListeners()

    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?)

    fun setRecycleChildrenOnDetach(recycle: Boolean)
}

