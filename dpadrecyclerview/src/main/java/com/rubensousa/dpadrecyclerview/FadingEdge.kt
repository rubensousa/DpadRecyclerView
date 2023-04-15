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

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Shader
import android.view.View
import androidx.core.view.forEach
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

    private var minBitmap: Bitmap? = null
    private var minShader: LinearGradient? = null
    private var maxBitmap: Bitmap? = null
    private var maxShader: LinearGradient? = null
    private val rect = Rect()
    private val paint = Paint().also { it.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN) }

    fun isEdgeFadingEnabled() = isFadingMinEdge || isFadingMaxEdge

    fun enableMinEdgeFading(enable: Boolean, recyclerView: DpadRecyclerView) {
        if (isFadingMinEdge == enable) return
        isFadingMinEdge = enable
        if (!isFadingMinEdge) {
            minBitmap = null
        }
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
        if (!isFadingMaxEdge) {
            maxBitmap = null
        }
        recyclerView.invalidate()
        updateLayerType(recyclerView)
    }

    fun setMaxEdgeFadingLength(length: Int, recyclerView: DpadRecyclerView) {
        if (maxShaderLength == length) return
        maxShaderLength = length
        maxShader = if (maxShaderLength != 0) {
            if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
                LinearGradient(
                    0f, 0f, maxShaderLength.toFloat(), 0f,
                    Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
            } else {
                LinearGradient(
                    0f, 0f, 0f, maxShaderLength.toFloat(),
                    Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP
                )
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
        recyclerView.forEach { child ->
            if (child.left < recyclerView.paddingLeft + minShaderOffset) {
                return true
            }
        }
        return false
    }

    fun isMaxFadingEdgeRequired(recyclerView: DpadRecyclerView): Boolean {
        if (!isFadingMaxEdge) {
            return false
        }
        val childCount = recyclerView.childCount
        for (i in childCount - 1 downTo 0) {
            val child = recyclerView.getChildAt(i)
            if (child.right > recyclerView.width - recyclerView.paddingRight - maxShaderOffset) {
                return true
            }
        }
        return false
    }

    fun clearMinBitmap() {
        minBitmap = null
    }

    fun clearMaxBitmap() {
        maxBitmap = null
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
        minFadeSize: Int,
        maxEdge: Int,
        maxFadeSize: Int,
        canvas: Canvas,
        recyclerView: DpadRecyclerView
    ) {
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            canvas.clipRect(minEdge + minFadeSize, 0, maxEdge - maxFadeSize, recyclerView.height)
        } else {
            canvas.clipRect(0, minEdge + minFadeSize, recyclerView.width, maxEdge - maxFadeSize)
        }
    }

    fun drawMin(
        edge: Int,
        tmpCanvas: Canvas,
        tmpBitmap: Bitmap,
        viewCanvas: Canvas,
        recyclerView: DpadRecyclerView
    ) {
        paint.shader = minShader
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            rect.top = 0
            rect.bottom = recyclerView.height
            rect.left = 0
            rect.right = minShaderLength
            tmpCanvas.drawRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                paint
            )
            viewCanvas.translate(edge.toFloat(), 0f)
            viewCanvas.drawBitmap(tmpBitmap, rect, rect, null)
            viewCanvas.translate(-edge.toFloat(), 0f)
        } else {
            rect.left = 0
            rect.right = recyclerView.width
            rect.top = 0
            rect.bottom = minShaderLength
            tmpCanvas.drawRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                paint
            )
            viewCanvas.translate(0f, edge.toFloat())
            viewCanvas.drawBitmap(tmpBitmap, rect, rect, null)
            viewCanvas.translate(0f, -edge.toFloat())
        }
    }

    fun drawMax(
        edge: Int,
        tmpCanvas: Canvas,
        tmpBitmap: Bitmap,
        viewCanvas: Canvas,
        recyclerView: DpadRecyclerView
    ) {
        paint.shader = maxShader
        if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            rect.top = 0
            rect.bottom = recyclerView.height
            rect.left = 0
            rect.right = maxShaderLength
            tmpCanvas.drawRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                paint
            )
            viewCanvas.translate(edge.toFloat() - maxShaderLength, 0f)
            viewCanvas.drawBitmap(tmpBitmap, rect, rect, null)
            viewCanvas.translate(-(edge.toFloat() - maxShaderLength), 0f)
        } else {
            rect.left = 0
            rect.right = recyclerView.width
            rect.top = recyclerView.height - maxShaderLength
            rect.bottom = maxShaderLength
            tmpCanvas.drawRect(
                rect.left.toFloat(),
                rect.top.toFloat(),
                rect.right.toFloat(),
                rect.bottom.toFloat(),
                paint
            )
            viewCanvas.translate(0f, edge.toFloat())
            viewCanvas.drawBitmap(tmpBitmap, rect, rect, null)
            viewCanvas.translate(0f, -(edge.toFloat() - maxShaderLength))
        }
    }

    fun getMinBitmap(recyclerView: DpadRecyclerView): Bitmap {
        return if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            getHorizontalMinBitmap(recyclerView)
        } else {
            getVerticalMinBitmap(recyclerView)
        }
    }

    private fun getHorizontalMinBitmap(recyclerView: DpadRecyclerView): Bitmap {
        var currentBitmap = minBitmap
        if (currentBitmap == null
            || currentBitmap.width != minShaderLength
            || currentBitmap.height != recyclerView.height
        ) {
            currentBitmap = Bitmap.createBitmap(
                minShaderLength, recyclerView.height, Bitmap.Config.ARGB_8888
            )
        }
        return requireNotNull(currentBitmap)
    }

    private fun getVerticalMinBitmap(recyclerView: DpadRecyclerView): Bitmap {
        var currentBitmap = minBitmap
        if (currentBitmap == null
            || currentBitmap.height != minShaderLength
            || currentBitmap.width != recyclerView.width
        ) {
            currentBitmap = Bitmap.createBitmap(
                recyclerView.width, minShaderLength, Bitmap.Config.ARGB_8888
            )
        }
        return requireNotNull(currentBitmap)
    }

    fun getMaxBitmap(recyclerView: DpadRecyclerView): Bitmap {
        return if (recyclerView.getOrientation() == RecyclerView.HORIZONTAL) {
            getHorizontalMaxBitmap(recyclerView)
        } else {
            getVerticalMaxBitmap(recyclerView)
        }
    }

    private fun getHorizontalMaxBitmap(recyclerView: DpadRecyclerView): Bitmap {
        var currentBitmap = maxBitmap
        if (currentBitmap == null
            || currentBitmap.width != maxShaderLength
            || currentBitmap.height != recyclerView.height
        ) {
            currentBitmap = Bitmap.createBitmap(
                maxShaderLength, recyclerView.height, Bitmap.Config.ARGB_8888
            )
        }
        return requireNotNull(currentBitmap)
    }

    private fun getVerticalMaxBitmap(recyclerView: DpadRecyclerView): Bitmap {
        var currentBitmap = maxBitmap
        if (currentBitmap == null
            || currentBitmap.height != maxShaderLength
            || currentBitmap.width != recyclerView.width
        ) {
            currentBitmap = Bitmap.createBitmap(
                recyclerView.width, maxShaderLength, Bitmap.Config.ARGB_8888
            )
        }
        return requireNotNull(currentBitmap)
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
