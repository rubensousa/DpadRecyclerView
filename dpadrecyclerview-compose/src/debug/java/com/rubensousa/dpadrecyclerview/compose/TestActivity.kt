/*
 * Copyright 2023 Rúben Sousa
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
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class TestActivity : AppCompatActivity() {

    private lateinit var recyclerView: DpadRecyclerView
    private val clicks = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_test)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.adapter = Adapter(List(100) { it })
        recyclerView.requestFocus()
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

    fun removeAdapter() {
        recyclerView.adapter = null
    }

    fun getViewsHolders(): List<RecyclerView.ViewHolder> {
        val viewHolders = ArrayList<RecyclerView.ViewHolder>()
        recyclerView.children.forEach { child ->
            viewHolders.add(recyclerView.getChildViewHolder(child))
        }
        return viewHolders
    }

    inner class Adapter(private val items: List<Int>) :
        RecyclerView.Adapter<DpadComposeViewHolder<Int>>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DpadComposeViewHolder<Int> {
            return DpadComposeViewHolder(parent,
                composable = { item, isFocused, isSelected ->
                    TestComposable(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        item = item,
                        isFocused = isFocused,
                        isSelected = isSelected
                    )
                },
                onClick = {
                    clicks.add(it)
                },
                isFocusable = true)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: DpadComposeViewHolder<Int>, position: Int) {
            holder.setItemState(items[position])
        }

    }
}