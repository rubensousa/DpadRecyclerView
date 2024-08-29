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

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.compose.test.R
import com.rubensousa.dpadrecyclerview.testfixtures.DpadFocusEvent

class ComposeFocusTestActivity : AppCompatActivity() {

    private lateinit var recyclerView: DpadRecyclerView
    private val focusEvents = arrayListOf<DpadFocusEvent>()
    private val clicks = ArrayList<Int>()
    private val longClicks = ArrayList<Int>()
    private val disposals = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_test)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.addOnViewFocusedListener(object : OnViewFocusedListener {
            override fun onViewFocused(parent: RecyclerView.ViewHolder, child: View) {
                focusEvents.add(DpadFocusEvent(parent, child, parent.layoutPosition))
            }
        })
        recyclerView.adapter = Adapter(
            items = List(100) { it },
            onDispose = { item ->
                disposals.add(item)
            }
        )
        recyclerView.requestFocus()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                clearFocus()
            }
        })
    }

    fun requestFocus() {
        recyclerView.requestFocus()
    }

    fun clearFocus() {
        findViewById<View>(R.id.focusPlaceholder).requestFocus()
    }

    fun getClicks(): List<Int> {
        return clicks
    }

    fun getLongClicks(): List<Int> {
        return clicks
    }

    fun getDisposals(): List<Int> {
        return disposals
    }

    fun getFocusEvents(): List<DpadFocusEvent> = focusEvents.toList()

    fun removeAdapter() {
        recyclerView.adapter = null
    }

    fun getRecyclerView(): DpadRecyclerView = recyclerView

    fun getViewsHolders(): List<RecyclerView.ViewHolder> {
        val viewHolders = ArrayList<RecyclerView.ViewHolder>()
        recyclerView.children.forEach { child ->
            viewHolders.add(recyclerView.getChildViewHolder(child))
        }
        return viewHolders
    }

    inner class Adapter(
        private val items: List<Int>,
        private val onDispose: (item: Int) -> Unit,
    ) : RecyclerView.Adapter<DpadComposeFocusViewHolder<Int>>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): DpadComposeFocusViewHolder<Int> {
            return DpadComposeFocusViewHolder(parent) { item ->
                TestComposableFocus(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    item = item,
                    onClick = {
                        clicks.add(item)
                    },
                    onLongClick = {
                        longClicks.add(item)
                    },
                    onDispose = {
                        onDispose(item)
                    }
                )
            }
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: DpadComposeFocusViewHolder<Int>, position: Int) {
            holder.setItemState(items[position])
        }

        override fun onViewRecycled(holder: DpadComposeFocusViewHolder<Int>) {
            holder.setItemState(null)
        }

    }
}