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

import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds

data class LayoutConfig(
    val parentWidth: Int,
    val parentHeight: Int,
    val viewWidth: Int,
    val viewHeight: Int,
    val defaultItemCount: Int,
    val parentKeyline: Int,
    val childKeyline: Float,
    val reversed: Boolean = false,
    val alignToStartEdge: Boolean = false,
    val decorInsets: ViewBounds = ViewBounds(0, 0, 0, 0)
)
