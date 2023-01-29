package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import androidx.recyclerview.widget.RecyclerView

internal class ItemChanges {

    var insertionPosition: Int = RecyclerView.NO_POSITION
    var insertionItemCount: Int = 0

    var removalPosition: Int = RecyclerView.NO_POSITION
    var removalItemCount: Int = 0

    var moveFromPosition: Int = RecyclerView.NO_POSITION
    var moveToPosition: Int = RecyclerView.NO_POSITION
    var moveItemCount: Int = 0

    fun isValid(): Boolean {
        val isInsertion = insertionPosition != RecyclerView.NO_POSITION
                && insertionItemCount > 0
        val isRemoval =  removalPosition != RecyclerView.NO_POSITION
                && removalPosition > 0
        val isMove = moveFromPosition != RecyclerView.NO_POSITION
                && moveToPosition != RecyclerView.NO_POSITION
                && moveItemCount > 0
        return isInsertion || isRemoval || isMove
    }

    fun reset() {
        insertionPosition = RecyclerView.NO_POSITION
        insertionItemCount = 0

        removalPosition = RecyclerView.NO_POSITION
        removalItemCount = 0

        moveFromPosition = RecyclerView.NO_POSITION
        moveToPosition = RecyclerView.NO_POSITION
        moveItemCount = 0
    }

    fun isOutOfBounds(firstPos: Int, lastPos: Int): Boolean {
        if (!isInsertionOutOfBounds(insertionPosition, insertionItemCount, firstPos, lastPos)) {
            return false
        }
        if (!isRangeOutOfBounds(removalPosition, removalItemCount, firstPos, lastPos)) {
            return false
        }
        val moveFromOutOfBounds = isRangeOutOfBounds(
            moveFromPosition, moveItemCount, firstPos, lastPos
        )
        val moveToOutOfBounds = isRangeOutOfBounds(
            moveToPosition, moveItemCount, firstPos, lastPos
        )
        if (!moveFromOutOfBounds && !moveToOutOfBounds) {
            return false
        }
        return true
    }

    private fun isInsertionOutOfBounds(
        positionStart: Int,
        itemCount: Int,
        firstPos: Int,
        lastPos: Int
    ): Boolean {
        if (positionStart == firstPos - 1) {
            return false
        }
        if (positionStart == lastPos + 1) {
            return false
        }
        return positionStart + itemCount < firstPos || positionStart > lastPos
    }

    private fun isRangeOutOfBounds(
        positionStart: Int,
        itemCount: Int,
        firstPos: Int,
        lastPos: Int
    ): Boolean {
        return positionStart + itemCount < firstPos || positionStart > lastPos
    }

    override fun toString(): String {
        return "ItemChanges(insertionPosition=$insertionPosition, " +
                "insertionItemCount=$insertionItemCount, " +
                "removalPosition=$removalPosition, " +
                "removalItemCount=$removalItemCount, " +
                "moveFromPosition=$moveFromPosition, " +
                "moveToPosition=$moveToPosition, " +
                "moveItemCount=$moveItemCount)"
    }


}
