package com.rubensousa.dpadrecyclerview.sample.ui.screen.text

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadScroller
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenTextScrollingBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class TextScrollingFragment : Fragment(R.layout.screen_text_scrolling) {

    private val binding by viewBinding(ScreenTextScrollingBinding::bind)
    private val scroller = DpadScroller(object : DpadScroller.ScrollDistanceCalculator {
        override fun calculateScrollDistance(
            recyclerView: DpadRecyclerView,
            event: KeyEvent
        ): Int {
            return recyclerView.height / 5
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TextAdapter()
        val text = view.resources.getString(R.string.placeholder_text)
        val list = mutableListOf<String>()
        repeat(20) {
            list.add(text)
        }
        adapter.submitList(list)
        binding.recyclerView.apply {
            this.adapter = adapter
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing)
                )
            )
        }
        scroller.attach(binding.recyclerView)
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.recyclerView.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scroller.detach()
    }

}
