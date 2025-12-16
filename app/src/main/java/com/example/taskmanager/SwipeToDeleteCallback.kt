package com.example.taskmanager

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

abstract class SwipeToDeleteCallback(context: Context) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val editIcon: Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_edit)
    private val deleteIcon: Drawable? = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_delete)

    private val editBackground = ColorDrawable()
    private val deleteBackground = ColorDrawable()
    private val editColor = Color.parseColor("#FFA500") // Orange
    private val deleteColor = Color.parseColor("#D32F2F") // Red

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
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
        val itemView = viewHolder.itemView
        val itemHeight = itemView.bottom - itemView.top

        if (dX > 0) { // Swiping to the right (Edit)
            editBackground.color = editColor
            editBackground.setBounds(
                itemView.left,
                itemView.top,
                itemView.left + dX.toInt(),
                itemView.bottom
            )
            editBackground.draw(c)

            val iconHeight = editIcon?.intrinsicHeight ?: 0
            val iconWidth = editIcon?.intrinsicWidth ?: 0
            val iconTop = itemView.top + (itemHeight - iconHeight) / 2
            val iconMargin = (itemHeight - iconHeight) / 2
            val iconLeft = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + iconWidth
            val iconBottom = iconTop + iconHeight

            editIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            editIcon?.draw(c)
        } else if (dX < 0) { // Swiping to the left (Delete)
            deleteBackground.color = deleteColor
            deleteBackground.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            deleteBackground.draw(c)

            val iconHeight = deleteIcon?.intrinsicHeight ?: 0
            val iconWidth = deleteIcon?.intrinsicWidth ?: 0
            val iconTop = itemView.top + (itemHeight - iconHeight) / 2
            val iconMargin = (itemHeight - iconHeight) / 2
            val iconLeft = itemView.right - iconMargin - iconWidth
            val iconRight = itemView.right - iconMargin
            val iconBottom = iconTop + iconHeight

            deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon?.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}