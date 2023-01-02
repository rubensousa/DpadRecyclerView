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

package com.rubensousa.dpadrecyclerview.testfixtures

import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.common.truth.Truth.assertThat

object LayoutManagerAssertions {

    fun assertChildrenBounds(layoutManager: LayoutManager, matrix: LayoutMatrix) {
        val horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        val verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        assertThat(layoutManager.childCount).isEqualTo(matrix.getViewsInLayout().size)

        for (i in 0 until layoutManager.childCount) {
            val view = layoutManager.getChildAt(i)!!
            val viewItem = matrix.getViewAt(i)
            assertThat(horizontalHelper.getDecoratedStart(view))
                .isEqualTo(viewItem.getDecoratedLeft())
            assertThat(horizontalHelper.getDecoratedEnd(view))
                .isEqualTo(viewItem.getDecoratedRight())
            assertThat(verticalHelper.getDecoratedStart(view))
                .isEqualTo(viewItem.getDecoratedTop())
            assertThat(verticalHelper.getDecoratedEnd(view))
                .isEqualTo(viewItem.getDecoratedBottom())
        }
    }

}
