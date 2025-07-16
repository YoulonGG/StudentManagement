package com.example.studentmanagement.presentation.subjects_list.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.studentmanagement.R

class SwipeToDeleteCallback(
    private val context: Context,
    private val onItemSwiped: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val deleteIcon: Drawable?
    private val intrinsicWidth: Int
    private val intrinsicHeight: Int
    private val background: ColorDrawable
    private val backgroundColor: Int

    init {
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.bin)
        intrinsicWidth = deleteIcon?.intrinsicWidth ?: 0
        intrinsicHeight = deleteIcon?.intrinsicHeight ?: 0
        backgroundColor = "#f44336".toColorInt()
        background = backgroundColor.toDrawable()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        onItemSwiped(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        when {
            dX < 0 -> {
                val iconMargin = (itemView.height - intrinsicHeight) / 2
                val iconTop = itemView.top + (itemView.height - intrinsicHeight) / 2
                val iconBottom = iconTop + intrinsicHeight

                val iconLeft = itemView.right - iconMargin - intrinsicWidth
                val iconRight = itemView.right - iconMargin

                deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )

                background.draw(c)
                deleteIcon?.draw(c)
            }
        }
    }
}