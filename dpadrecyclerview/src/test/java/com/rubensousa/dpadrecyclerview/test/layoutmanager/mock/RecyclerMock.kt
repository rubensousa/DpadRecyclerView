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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.mock

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.mockk.every
import io.mockk.mockk

internal class RecyclerMock(
    private val viewAdapter: TestViewAdapter
) {

    private val mock = mockk<RecyclerView.Recycler>()
    private var scrapList : List<TestViewAdapter.ViewHolder> = emptyList()

    init {
        every { mock.recycleView(any()) }.answers { }
        every { mock.getViewForPosition(any()) }.answers {
            viewAdapter.getViewAt(it.invocation.args.first() as Int)!!
        }
        every { mock.scrapList }.answers { scrapList }
    }

    fun get(): RecyclerView.Recycler = mock

    fun getItemCount() = viewAdapter.getItemCount()

    fun setScrap(views: List<View>) {
        scrapList = List(views.size) { index ->
            TestViewAdapter.ViewHolder(views[index])
        }
    }

}
