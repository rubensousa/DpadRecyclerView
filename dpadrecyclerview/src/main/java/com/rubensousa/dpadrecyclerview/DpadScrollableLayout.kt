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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
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
 * Use `app:dpadScrollableLayoutScrollableView=true` in the scrollable view
 * that should fill the entire parent.
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

    var isHeaderVisible = false
        private set

    private var currentOffset = 0
    private var offsetInProgress: Int? = null
    private var headerHeightChanged = false
    private var currentAnimator: ScrollAnimator? = null
    private var scrollDurationConfig: ScrollDurationConfig = DefaultScrollDurationConfig()
    private var lastHeaderHeight = 0

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
        var newHeaderHeight = 0
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view != null) {
                val layoutParams = view.layoutParams as LayoutParams
                if (layoutParams.isScrollableView) {
                    childHeight += matchParentHeight
                } else {
                    newHeaderHeight += view.measuredHeight
                    childHeight += view.measuredHeight
                }
            }
        }
        setMeasuredDimension(measuredWidth, childHeight)
        if (newHeaderHeight != headerHeight) {
            headerHeightChanged = newHeaderHeight != lastHeaderHeight
            lastHeaderHeight = headerHeight
            headerHeight = newHeaderHeight
        }
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
        var currentAnchor = if (headerHeightChanged && lastHeaderHeight > 0) {
            if (isHeaderVisible) {
                0
            } else {
                -headerHeight
            }
        } else {
            currentOffset
        }
        val numberOfChildren = childCount
        for (i in 0 until numberOfChildren) {
            val child = getChildAt(i) ?: continue
            if (child.visibility == View.GONE) {
                continue
            }
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val layoutParams = child.layoutParams as LayoutParams
            val childLeft = paddingLeft + layoutParams.leftMargin
            val childTop = currentAnchor + layoutParams.topMargin
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
            currentAnchor += childHeight + layoutParams.bottomMargin
        }
        headerHeightChanged = false
    }

    fun setScrollInterpolator(interpolator: Interpolator) {
        scrollInterpolator = interpolator
    }

    fun setScrollDurationConfig(config: ScrollDurationConfig) {
        scrollDurationConfig = config
    }

    fun showHeader(smooth: Boolean = true) {
        scrollHeaderTo(0, smooth)
    }

    fun hideHeader(smooth: Boolean = true) {
        scrollHeaderTo(-headerHeight, smooth)
    }

    fun scrollHeaderTo(topOffset: Int, smooth: Boolean = true) {
        val targetOffset = max(topOffset, -headerHeight)
        // Do nothing if we're already animating or moving the offset to the target passed
        if (offsetInProgress == targetOffset || currentOffset == targetOffset) {
            return
        }
        cancelOffsetAnimation()
        // If we haven't been laid out yet, postpone the offset until we are
        if (height == 0 || !isLaidOut) {
            return
        }
        if (smooth) {
            offsetInProgress = targetOffset
            val animator = createAnimator(
                targetOffset = targetOffset,
                currentOffset = currentOffset
            )
            currentAnimator = animator
            animator.start()
        } else {
            offsetTo(targetOffset)
        }
    }

    private fun createAnimator(targetOffset: Int, currentOffset: Int): ScrollAnimator {
        val layoutHeight = if (height - headerHeight > 0) {
            height - headerHeight
        } else {
            height
        }
        return ScrollAnimator(
            scrollInterpolator = scrollInterpolator,
            scrollDuration = scrollDurationConfig.calculateScrollDuration(
                layoutHeight = layoutHeight,
                dy = targetOffset - currentOffset
            ),
            onUpdate = { fraction ->
                offset(fraction = fraction, initial = currentOffset, target = targetOffset)
            },
            onEnd = {
                offsetInProgress = null
                currentAnimator = null
            }
        )
    }

    private fun offset(fraction: Float, initial: Int, target: Int) {
        val nextOffset = initial + (target - initial) * fraction
        val diff = nextOffset - currentOffset
        offsetBy(diff.toInt())
    }

    private fun offsetTo(targetOffset: Int) {
        offsetBy(-(currentOffset - targetOffset))
    }

    private fun offsetBy(dy: Int) {
        for (i in 0 until childCount) {
            getChildAt(i)?.offsetTopAndBottom(dy)
        }
        currentOffset = getChildAt(0)?.top ?: 0
        isHeaderVisible = currentOffset > -headerHeight
    }

    private fun cancelOffsetAnimation() {
        currentAnimator?.cancel()
        offsetInProgress = null
        currentAnimator = null
    }

    private class DefaultScrollDurationConfig : ScrollDurationConfig {
        // From RecyclerView to make scrolling as similar as possible
        override fun calculateScrollDuration(layoutHeight: Int, dy: Int): Int {
            val absDy = abs(dy.toDouble()).toInt()
            val duration = (((absDy / layoutHeight) + 1) * 300)
            return min(duration.toDouble(), 2000.0).toInt()
        }
    }

    interface ScrollDurationConfig {
        fun calculateScrollDuration(layoutHeight: Int, dy: Int): Int
    }

    private class ScrollAnimator(
        private val scrollInterpolator: Interpolator,
        private val scrollDuration: Int,
        private val onUpdate: (fraction: Float) -> Unit,
        private val onEnd: () -> Unit,
    ) : ValueAnimator.AnimatorUpdateListener {

        private val animator = ValueAnimator()
        private var canceled = false

        init {
            animator.apply {
                setDuration(scrollDuration.toLong())
                interpolator = scrollInterpolator
                addUpdateListener(this@ScrollAnimator)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        if (!canceled) {
                            onEnd()
                        }
                    }
                })
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
