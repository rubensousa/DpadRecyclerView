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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.drag

import android.os.Bundle
import android.view.View
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenDragDropBinding
import com.rubensousa.dpadrecyclerview.sample.ui.dpToPx
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import kotlinx.coroutines.flow.MutableStateFlow

class DragAndDropFragment : Fragment(R.layout.screen_drag_drop) {

    private val binding by viewBinding(ScreenDragDropBinding::bind)
    private val dragState = MutableStateFlow<Int?>(null)
    private val dragAdapter = DragAdapter(dragState,
        onDragStart = { viewHolder ->
            startDrag(viewHolder)
        }
    )
    private val dragHelper = DpadDragHelper(object : DpadDragHelper.DragCallback {

        override fun move(
            src: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            dragAdapter.move(
                from = src.bindingAdapterPosition,
                to = target.bindingAdapterPosition
            )
            return true
        }

        override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
            dragState.value = dragAdapter.getItem(viewHolder.bindingAdapterPosition)
        }

        override fun onDragStopped() {
            dragState.value = null
        }

    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.apply {
            adapter = dragAdapter
            dragHelper.attachRecyclerView(this)
            itemAnimator = DefaultItemAnimator().apply {
                // For faster moves
                moveDuration = 100
            }
            setOrientation(RecyclerView.HORIZONTAL)
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = dpToPx(16.dp),
                    edgeSpacing = dpToPx(48.dp),
                    perpendicularEdgeSpacing = dpToPx(48.dp)
                )
            )
            dragHelper.attachRecyclerView(this)
        }
        dragAdapter.submitList(List(20) { it }.toMutableList())

        binding.dragButton.requestFocus()
        binding.dragButton.setContent {
            val focusRequester = remember { FocusRequester() }
            DragButtonItem(
                isDragging = dragState.collectAsStateWithLifecycle().value != null,
                onStartDragClick = { startDrag() },
                onStopDragClick = { stopDrag() },
                focusRequester = focusRequester
            )
            LaunchedEffect(key1 = Unit) {
                focusRequester.requestFocus()
            }
        }
    }

    private fun startDrag(viewHolder: RecyclerView.ViewHolder) {
        dragHelper.startDrag(viewHolder.absoluteAdapterPosition)
    }

    private fun startDrag() {
        dragHelper.startDrag(position = 0)
    }

    private fun stopDrag() {
        dragHelper.stopDrag()
    }

}
