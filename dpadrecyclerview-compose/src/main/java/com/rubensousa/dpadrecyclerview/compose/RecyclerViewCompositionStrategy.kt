/*
 * Copyright 2024 Rúben Sousa
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
package com.rubensousa.dpadrecyclerview.compose

import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.customview.poolingcontainer.PoolingContainerListener
import androidx.customview.poolingcontainer.addPoolingContainerListener
import androidx.customview.poolingcontainer.removePoolingContainerListener
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

object RecyclerViewCompositionStrategy {

    /**
     * Similar to [ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool] but skips
     * releasing compositions when detached from window. This is useful for re-using compositions
     * a lot more often when scrolling a RecyclerView.
     *
     * If you use [DpadRecyclerView.setRecycleChildrenOnDetach],
     * this will behave exactly the same as [ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool].
     *
     * If you use [DpadRecyclerView.setExtraLayoutSpaceStrategy],
     * please profile the compositions before considering using this strategy.
     */
    object DisposeOnRecycled : ViewCompositionStrategy {
        override fun installFor(view: AbstractComposeView): () -> Unit {
            val poolingContainerListener = PoolingContainerListener {
                view.disposeComposition()
            }
            view.addPoolingContainerListener(poolingContainerListener)
            return {
                view.removePoolingContainerListener(poolingContainerListener)
            }
        }
    }

}
