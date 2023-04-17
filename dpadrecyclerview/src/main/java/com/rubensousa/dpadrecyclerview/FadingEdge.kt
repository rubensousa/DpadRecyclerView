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

package com.rubensousa.dpadrecyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.view.View
import androidx.recyclerview.widget.RecyclerView

internal class FadingEdge {

    var isFadingMinEdge = false
        private set

    var minShaderLength = 0
        private set

    var minShaderOffset = 0
        private set

    var isFadingMaxEdge = false
        private set

    var maxShaderLength = 0
        private set

    var maxShaderOffset = 0
        private set

    private var minShader: LinearGradient? = null
    private var maxShader: LinearGradient? = null
    private val rect = Rect()
    private val paint = Paint()

    init {
        paint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            isDither = true
        }
    }

    fun onSizeChanged(
        width: Int,
        height: Int,
        oldWidth: Int,
        oldHeight: Int,
        recyclerView: DpadRecyclerView
    ) {
        if (maxShaderLength == 0) return
        var changed = false
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            if (width != oldWidth) {
                maxShader = createMaxHorizontalShader(width, recyclerView.paddingRight)
                changed = true
            }
        } else if (height != oldHeight) {
            maxShader = createMaxVerticalShader(height, recyclerView.paddingBottom)
            changed = true
        }
        if (changed) {
            recyclerView.invalidate()
        }
    }

    fun enableMinEdgeFading(enable: Boolean, recyclerView: DpadRecyclerView) {
        if (isFadingMinEdge == enable) return
        isFadingMinEdge = enable
        recyclerView.invalidate()
        updateLayerType(recyclerView)
    }

    fun setMinEdgeFadingLength(length: Int, recyclerView: DpadRecyclerView) {
        if (minShaderLength == length) return
        minShaderLength = length
        minShader = if (minShaderLength != 0) {
            if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
                LinearGradient(
                    0f, 0f, minShaderLength.toFloat(), 0f,
                    Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP
                )
            } else {
                LinearGradient(
                    0f, 0f, 0f, minShaderLength.toFloat(),
                    Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP
                )
            }
        } else {
            null
        }
        recyclerView.invalidate()
    }

    fun setMinEdgeFadingOffset(offset: Int, recyclerView: DpadRecyclerView) {
        if (minShaderOffset != offset) {
            minShaderOffset = offset
            recyclerView.invalidate()
        }
    }

    fun enableMaxEdgeFading(enable: Boolean, recyclerView: DpadRecyclerView) {
        if (isFadingMaxEdge == enable) return
        isFadingMaxEdge = enable
        recyclerView.invalidate()
        updateLayerType(recyclerView)
    }

    fun setMaxEdgeFadingLength(length: Int, recyclerView: DpadRecyclerView) {
        if (maxShaderLength == length) return
        maxShaderLength = length
        maxShader = if (maxShaderLength != 0) {
            if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
                createMaxHorizontalShader(recyclerView.width, recyclerView.paddingRight)
            } else {
                createMaxVerticalShader(recyclerView.height, recyclerView.paddingBottom)
            }
        } else {
            null
        }
        recyclerView.invalidate()
    }

    fun setMaxEdgeFadingOffset(offset: Int, recyclerView: DpadRecyclerView) {
        if (maxShaderOffset != offset) {
            maxShaderOffset = offset
            recyclerView.invalidate()
        }
    }

    fun isMinFadingEdgeRequired(recyclerView: DpadRecyclerView): Boolean {
        if (!isFadingMinEdge) {
            return false
        }
        val childCount = recyclerView.childCount
        if (childCount == 0) return false
        val child = recyclerView.getChildAt(0)
        val isHorizontal = recyclerView.getOrientation() == RecyclerView.HORIZONTAL
        val first = isFirstItemView(child, recyclerView)
        val childStart: Int
        val start: Int
        if (isHorizontal) {
            childStart = child.left
            start = recyclerView.paddingLeft
        } else {
            childStart = child.top
            start = recyclerView.paddingTop
        }
        return (childStart < start + minShaderOffset && !first) || (childStart < start && first)
    }

    fun isMaxFadingEdgeRequired(recyclerView: DpadRecyclerView): Boolean {
        if (!isFadingMaxEdge) {
            return false
        }
        val childCount = recyclerView.childCount
        if (childCount == 0) return false
        val isHorizontal = recyclerView.getOrientation() == RecyclerView.HORIZONTAL
        val child = recyclerView.getChildAt(childCount - 1)
        val last = isLastItemView(child, recyclerView)
        val childEnd: Int
        val end: Int
        if (isHorizontal) {
            childEnd = child.right
            end = recyclerView.width - recyclerView.paddingRight
        } else {
            childEnd = child.bottom
            end = recyclerView.height - recyclerView.paddingBottom
        }
        return (childEnd > end - maxShaderOffset && !last) || (childEnd > end && last)
    }

    fun getMinEdge(recyclerView: DpadRecyclerView): Int {
        if (!isFadingMinEdge) return 0
        val padding = if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            recyclerView.paddingLeft
        } else {
            recyclerView.paddingTop
        }
        return padding + minShaderOffset
    }

    fun getMaxEdge(recyclerView: DpadRecyclerView): Int {
        var padding = 0
        var size = 0
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            padding = recyclerView.paddingRight
            size = recyclerView.width
        } else {
            padding = recyclerView.paddingBottom
            size = recyclerView.height
        }
        if (!isFadingMaxEdge) return size
        return size - padding - maxShaderOffset
    }

    fun clip(
        minEdge: Int,
        maxEdge: Int,
        applyMinFading: Boolean,
        applyMaxFading: Boolean,
        canvas: Canvas,
        recyclerView: DpadRecyclerView
    ) {
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            val start = if (applyMinFading) minEdge else 0
            val end = if (applyMaxFading) maxEdge else recyclerView.width
            canvas.clipRect(start, 0, end, recyclerView.height)
        } else {
            val top = if (applyMinFading) minEdge else 0
            val bottom = if (applyMaxFading) maxEdge else recyclerView.height
            canvas.clipRect(0, top, recyclerView.width, bottom)
        }
    }

    fun drawMin(canvas: Canvas, recyclerView: DpadRecyclerView) {
        paint.shader = minShader
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            rect.top = 0
            rect.bottom = recyclerView.height
            rect.left = minShaderOffset
            rect.right = minShaderOffset + minShaderLength
        } else {
            rect.left = 0
            rect.right = recyclerView.width
            rect.top = minShaderOffset
            rect.bottom = minShaderOffset + minShaderLength
        }
        canvas.drawRect(rect, paint)
    }

    fun drawMax(canvas: Canvas, recyclerView: DpadRecyclerView) {
        paint.shader = maxShader
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            rect.top = 0
            rect.bottom = recyclerView.height
            rect.right = recyclerView.width - recyclerView.paddingRight - maxShaderOffset
            rect.left = rect.right - maxShaderLength
        } else {
            rect.left = 0
            rect.right = recyclerView.width
            rect.bottom = recyclerView.height - recyclerView.paddingBottom - maxShaderOffset
            rect.top = rect.bottom - maxShaderLength
        }
        canvas.drawRect(rect, paint)
    }

    private fun isFirstItemView(view: View, recyclerView: DpadRecyclerView): Boolean {
        return recyclerView.getChildLayoutPosition(view) == 0
    }

    private fun isLastItemView(view: View, recyclerView: DpadRecyclerView): Boolean {
        val itemCount = recyclerView.adapter?.itemCount ?: 0
        return recyclerView.getChildLayoutPosition(view) == itemCount - 1
    }

    private fun createMaxHorizontalShader(width: Int, paddingEnd: Int): LinearGradient {
        val end = width.toFloat() - paddingEnd.toFloat() - maxShaderOffset
        return LinearGradient(
            end - maxShaderLength, 0f, end, 0f,
            Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
    }

    private fun createMaxVerticalShader(height: Int, paddingBottom: Int): LinearGradient {
        val bottom = height.toFloat() - paddingBottom.toFloat() - maxShaderOffset
        return LinearGradient(
            0f, bottom - maxShaderLength, 0f, bottom,
            Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
        )
    }

    private fun updateLayerType(recyclerView: DpadRecyclerView) {
        if (isFadingMinEdge || isFadingMaxEdge) {
            recyclerView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            recyclerView.setWillNotDraw(false)
        } else {
            recyclerView.setLayerType(View.LAYER_TYPE_NONE, null)
            recyclerView.setWillNotDraw(true)
        }
    }

}
