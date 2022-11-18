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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

/**
 * Direction in which the layout is being filled.
 * These are absolute directions, so it doesn't consider RTL at all
 */
internal enum class LayoutDirection(val value: Int) {
    /**
     * Either left in horizontal or top in vertical
     */
    START(-1),

    /**
     * Either right in horizontal or bottom in vertical
     */
    END(1)
}