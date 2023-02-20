package com.rubensousa.dpadrecyclerview.sample.ui.widgets.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.material.shape.MaterialShapeDrawable
import com.rubensousa.dpadrecyclerview.sample.R

class SelectionOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val materialBackground = MaterialShapeDrawable()

    init {
        materialBackground.setCornerSize(
            resources.getDimensionPixelOffset(R.dimen.corner_size).toFloat()
        )
        materialBackground.strokeColor = AppCompatResources.getColorStateList(
            context,
            R.color.selection_overlay
        )
        materialBackground.fillColor = ColorStateList.valueOf(Color.TRANSPARENT)
        materialBackground.strokeWidth = resources.getDimensionPixelOffset(
            R.dimen.selection_overlay_stroke_width
        ).toFloat()
        materialBackground.elevation = 2f
        background = materialBackground
    }

}
