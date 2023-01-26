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

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.view.FocusFinder
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

/**
 * Implementation for [FocusableDirection.STANDARD]
 */
internal class DefaultFocusInterceptor(
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
    private val focusFinder: FocusFinder = FocusFinder.getInstance()
) : FocusInterceptor {

    override fun findFocus(
        recyclerView: RecyclerView,
        focusedView: View,
        position: Int,
        direction: Int
    ): View? {
        val absoluteDirection = FocusDirection.getAbsoluteDirection(
            direction = direction,
            isVertical = configuration.isVertical(),
            isRTL = layoutInfo.isRTL()
        )
        return focusFinder.findNextFocus(recyclerView, focusedView, absoluteDirection)
    }

}
