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

package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Task that's scheduled and executed when a ViewHolder is selected
 *
 * @param executeWhenAligned if this task should only be executed
 * when a ViewHolder is aligned to its final position,
 * or **false** if it should be executed immediately after the selection
 */
abstract class ViewHolderTask(val executeWhenAligned: Boolean = false) {
    abstract fun execute(viewHolder: RecyclerView.ViewHolder)
}
