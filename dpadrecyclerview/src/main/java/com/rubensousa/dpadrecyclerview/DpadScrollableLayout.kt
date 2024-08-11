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

package com.rubensousa.dpadrecyclerview

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A layout that behaves similarly to `AppBarLayout` inside a `CoordinatorLayout`
 * but with the caveat that nested scrolling is simulated and not actually real.
 *
 * Use it when you need a fixed header at the top of a [DpadRecyclerView] that can scroll away,
 * while supporting recycling at the same time.
 *
 * Use [scrollHeaderTo] to scroll the layout to a specific top offset.
 *
 * To scroll the header away, use [hideHeader].
 *
 * To show it back again, use [showHeader]
 */
class DpadScrollableLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    var headerHeight = 0
        private set

    private var pendingOffset: Int? = null
    private var currentAnimator: ScrollAnimator? = null

    // From RecyclerView
    private var scrollInterpolator = Interpolator { t ->
        var output = t
        output -= 1.0f
        output * output * output * output * output + 1.0f
    }

    init {
        // Only supports vertical layouts
        orientation = VERTICAL
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.EXACTLY
            )
        )
        val matchParentHeight = measuredHeight
        super.onMeasure(
            widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(heightMeasureSpec),
                MeasureSpec.UNSPECIFIED
            )
        )
        var childHeight = 0
        headerHeight = 0
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view != null) {
                val layoutParams = view.layoutParams as LayoutParams
                if (layoutParams.isScrollableView) {
                    childHeight += matchParentHeight
                } else {
                    headerHeight += view.measuredHeight
                    childHeight += view.measuredHeight
                }
            }
        }
        setMeasuredDimension(measuredWidth, childHeight)
    }

    override fun measureChildWithMargins(
        child: View?,
        parentWidthMeasureSpec: Int,
        widthUsed: Int,
        parentHeightMeasureSpec: Int,
        heightUsed: Int
    ) {
        val lp = child!!.layoutParams as LayoutParams
        val childWidthMeasureSpec = getChildMeasureSpec(
            parentWidthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin + widthUsed,
            lp.width
        )
        val verticalPadding =
            (paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin + heightUsed)
        val childHeightMeasureSpec = if (lp.isScrollableView) {
            getChildMeasureSpec(
                MeasureSpec.EXACTLY,
                verticalPadding,
                measuredHeight
            )
        } else {
            getChildMeasureSpec(
                parentHeightMeasureSpec,
                verticalPadding,
                lp.height
            )
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        pendingOffset?.let {
            if (height != 0) {
                offsetTopAndBottom(it)
                pendingOffset = null
            }
        }
    }

    fun setScrollInterpolator(interpolator: Interpolator) {
        scrollInterpolator = interpolator
    }

    fun showHeader(smooth: Boolean = true) {
        scrollHeaderTo(0, smooth)
    }

    fun hideHeader(smooth: Boolean = true) {
        scrollHeaderTo(-headerHeight, smooth)
    }

    fun scrollHeaderTo(topOffset: Int, smooth: Boolean = true) {
        val targetOffset = max(topOffset, -headerHeight)
        val currentOffset = top
        if (currentOffset == targetOffset) {
            return
        }
        if (height == 0 || !isLaidOut) {
            pendingOffset = topOffset
            return
        }
        currentAnimator?.cancel()
        if (smooth) {
            val animator = ScrollAnimator(
                scrollInterpolator = scrollInterpolator,
                scrollDuration = computeScrollDuration(targetOffset - currentOffset),
                onUpdate = { fraction ->
                    offset(fraction = fraction, initial = currentOffset, target = targetOffset)
                }
            )
            currentAnimator = animator
            animator.start()
        } else {
            offsetTopAndBottom(topOffset - top)
        }
    }

    // From RecyclerView
    private fun computeScrollDuration(dy: Int): Long {
        val absDy = abs(dy.toDouble()).toInt()
        val containerSize = height
        val duration = (((absDy / containerSize) + 1) * 300)
        return min(duration.toDouble(), 2000.0).toLong()
    }

    private fun offset(fraction: Float, initial: Int, target: Int) {
        val currentTop = top
        val nextOffset = initial + (target - initial) * fraction
        val diff = nextOffset - currentTop
        offsetTopAndBottom(diff.toInt())
    }

    private class ScrollAnimator(
        private val scrollInterpolator: Interpolator,
        private val scrollDuration: Long,
        private val onUpdate: (fraction: Float) -> Unit
    ) : ValueAnimator.AnimatorUpdateListener {

        private val animator = ValueAnimator()
        private var canceled = false

        init {
            animator.apply {
                setDuration(scrollDuration)
                interpolator = scrollInterpolator
                addUpdateListener(this@ScrollAnimator)
                setFloatValues(0f, 1f)
            }
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            if (canceled) {
                return
            }
            val value = animation.animatedValue as Float
            onUpdate(value)
        }

        fun start() {
            animator.start()
        }

        fun cancel() {
            canceled = true
            animator.cancel()
        }

    }

    class LayoutParams : LinearLayout.LayoutParams {

        var isScrollableView = false
            private set

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            val typedArray: TypedArray = context.obtainStyledAttributes(
                attrs, R.styleable.DpadScrollableLayout_Layout
            )
            isScrollableView = typedArray.getBoolean(
                R.styleable.DpadScrollableLayout_Layout_dpadScrollableLayoutScrollableView,
                false
            )
            typedArray.recycle()
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: MarginLayoutParams) : super(source)
        constructor(source: ViewGroup.LayoutParams) : super(source)

        fun setIsScrollableView(enable: Boolean) {
            isScrollableView = enable
        }
    }

}
