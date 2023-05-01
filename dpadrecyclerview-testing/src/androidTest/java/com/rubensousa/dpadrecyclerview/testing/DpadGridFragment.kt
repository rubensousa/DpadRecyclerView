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

package com.rubensousa.dpadrecyclerview.testing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.testfixtures.DpadSelectionEvent

class DpadGridFragment : Fragment(R.layout.dpadrecyclerview_test_container),
    OnViewHolderSelectedListener {

    private val selectionEvents = ArrayList<DpadSelectionEvent>()
    private val adapter = DpadTestAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.requestFocus()
        recyclerView.setParentAlignment(
            ParentAlignment(
                edge = ParentAlignment.Edge.NONE
            )
        )
        recyclerView.setFocusableDirection(FocusableDirection.CONTINUOUS)
        recyclerView.setSpanCount(5)
        recyclerView.addOnViewHolderSelectedListener(this)
        recyclerView.adapter = adapter
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelected(parent, child, position, subPosition)
        selectionEvents.add(DpadSelectionEvent(position, subPosition))
    }

    fun getAdapterSize() = adapter.itemCount

    fun insertItem() {
        postAction { adapter.addItem() }
    }

    fun removeItem() {
        postAction { adapter.removeItem() }
    }

    fun clearItems() {
        postAction { adapter.clearItems() }
    }

    fun changeLastItem() {
        postAction { adapter.changeLastItem() }
    }

    fun moveLastItem() {
        postAction { adapter.moveLastItem() }
    }

    private fun postAction(action: () -> Unit) {
        view?.postDelayed(action, 1000L)
    }

}
