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
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenDragDropBinding
import com.rubensousa.dpadrecyclerview.sample.ui.dpToPx
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DragAndDropFragment : Fragment(R.layout.screen_drag_drop) {

    private val binding by viewBinding(ScreenDragDropBinding::bind)
    private val dragState = MutableStateFlow<Int?>(null)
    private val dragAdapter = DragAdapter(
        dragState = dragState,
        onDragStart = { viewHolder ->
            startDrag(viewHolder)
        }
    )
    private val dragHelper = DpadDragHelper(
        adapter = dragAdapter,
        callback = object : DpadDragHelper.DragCallback {
            override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
                dragState.value = dragAdapter.getItem(viewHolder.bindingAdapterPosition)
            }

            override fun onDragStopped(fromUser: Boolean) {
                if (fromUser) {
                    // User requested dragging to stop
                } else {
                    // Dragging was stopped manually
                }
                dragState.value = null
            }
        }
    )
    private val backPressCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            dragHelper.stopDrag()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setDragButtonContent()
        dragAdapter.submitMutableList(List(20) { it }.toMutableList())
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressCallback)
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dragState.collect { draggingItem ->
                    backPressCallback.isEnabled = draggingItem != null
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = dragAdapter
            dragHelper.attachToRecyclerView(this)
            itemAnimator = DefaultItemAnimator().apply {
                // For faster moves
                moveDuration = 100
            }
            setOrientation(RecyclerView.HORIZONTAL)
            setItemSpacing(dpToPx(16.dp))
            setItemEdgeSpacing(dpToPx(48.dp))
        }
    }

    private fun setDragButtonContent() {
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
        dragHelper.startDrag(position = binding.recyclerView.getSelectedPosition())
    }

    private fun stopDrag() {
        dragHelper.stopDrag()
    }

}
