package com.rubensousa.dpadrecyclerview.test.layoutmanager.layout

import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemChanges
import org.junit.Test

class ItemChangesTest {

    private val changes = ItemChanges()

    @Test
    fun `insertion is out of bounds`() {
        changes.insertionPosition = 6
        changes.insertionItemCount = 1

        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 4)).isTrue()

        changes.insertionPosition = 11
        changes.insertionItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 9)).isTrue()
    }

    @Test
    fun `insertion within bounds`() {
        changes.insertionPosition = 5
        changes.insertionItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.insertionPosition = 0
        changes.insertionItemCount = 5
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.insertionPosition = 8
        changes.insertionItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.insertionPosition = 8
        changes.insertionItemCount = 10
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()
    }

    @Test
    fun `insertion at edges is within bounds`() {
        changes.insertionPosition = 5
        changes.insertionItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 4)).isFalse()

        changes.insertionPosition = 2
        changes.insertionItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 4)).isFalse()
    }

    @Test
    fun `removal is out of bounds`() {
        changes.removalPosition = 6
        changes.removalItemCount = 1

        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 4)).isTrue()

        changes.removalPosition = 10
        changes.removalItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 9)).isTrue()
    }

    @Test
    fun `removal within bounds`() {
        changes.removalPosition = 5
        changes.removalItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.removalPosition = 0
        changes.removalItemCount = 5
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.removalPosition = 8
        changes.removalItemCount = 1
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()

        changes.removalPosition = 8
        changes.removalItemCount = 10
        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isFalse()
    }

    @Test
    fun `move out of bounds`() {
        changes.moveFromPosition = 3
        changes.moveToPosition = 4
        changes.moveItemCount = 1

        assertThat(changes.isOutOfBounds(firstPos = 5, lastPos = 10)).isTrue()

        changes.moveFromPosition = 2
        changes.moveToPosition = 6
        changes.moveItemCount = 4
        assertThat(changes.isOutOfBounds(firstPos = 20, lastPos = 30)).isTrue()
    }

    @Test
    fun `move within bounds`() {
        changes.moveFromPosition = 3
        changes.moveToPosition = 4
        changes.moveItemCount = 1

        assertThat(changes.isOutOfBounds(firstPos = 3, lastPos = 6)).isFalse()

        changes.moveFromPosition = 2
        changes.moveToPosition = 6
        changes.moveItemCount = 4
        assertThat(changes.isOutOfBounds(firstPos = 4, lastPos = 10)).isFalse()
    }

}
