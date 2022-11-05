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

enum class FocusableDirection {
    /**
     * Focus goes to the next left, top, right or bottom view available
     */
    STANDARD,

    /**
     * Similar to [STANDARD], but applies a different logic for the edges:
     * * If the current focus is in the last column, when pressing right, the focus goes to the leftmost column
     * * If the current focus is in the first column, when pressing left, the focus goes to the rightmost column
     */
    CIRCULAR,

    /**
     * Similar to [STANDARD], but applies a different logic at the edges:
     * * If focusing forward (e.g Dpad right on a vertical grid) on the last column,
     * the next focused item will be the item in the first column of the next row.
     * * If focusing backwards (e.g Dpad left on a vertical grid) on the first column,
     * the next focused item will be the item in the last column of the previous row.
     */
    CONTINUOUS,
}