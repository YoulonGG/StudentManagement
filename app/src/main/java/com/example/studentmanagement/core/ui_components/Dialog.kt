package com.example.studentmanagement.core.ui_components

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.studentmanagement.R

/**
 * @Author: John Youlong.
 * @Date: 6/21/25.
 * @Email: johnyoulong@gmail.com.
 */


object Dialog {

    /**
     * Show a simple custom dialog with title and description
     * @param context Context (Activity/Fragment context)
     * @param title Dialog title
     * @param description Dialog description
     * @param buttonText Button text (default: "Okay")
     */
    fun showDialog(
        context: Context,
        title: String,
        description: String,
        buttonText: String = "Okay"
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_dialog, null)

        val txtTitle = view.findViewById<TextView>(R.id.dialog_title)
        val txtDescription = view.findViewById<TextView>(R.id.dialog_description)
        val btnOkay = view.findViewById<Button>(R.id.dialog_btn_okay)

        txtTitle.text = title
        txtDescription.text = description
        btnOkay.text = buttonText

        btnOkay.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    /**
     * Show a chip message at the top of the screen
     * @param context Context (Activity context)
     * @param title Chip title
     * @param description Chip description
     * @param type Message type (SUCCESS, ERROR, WARNING, INFO)
     */
    fun showChipMessage(
        context: Context,
        title: String,
        description: String,
        type: ChipType = ChipType.INFO
    ) {
        if (context !is Activity) return

        val inflater = LayoutInflater.from(context)
        val chipView = inflater.inflate(R.layout.custom_chip_message, null)

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
//            .apply {
//            if (this is ViewGroup.MarginLayoutParams) {
//                // Add margins if needed
//                topMargin = context.resources.getDimensionPixelSize(R.dimen.chip_margin_top)
//                leftMargin = context.resources.getDimensionPixelSize(R.dimen.chip_margin_horizontal)
//                rightMargin = context.resources.getDimensionPixelSize(R.dimen.chip_margin_horizontal)
//            }
//        }

        val rootView = context.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(chipView, params)

        val titleText = chipView.findViewById<TextView>(R.id.chip_title)
        val descriptionText = chipView.findViewById<TextView>(R.id.chip_description)
        val icon = chipView.findViewById<ImageView>(R.id.chip_icon)
        val container = chipView.findViewById<View>(R.id.chip_container)

        titleText.text = title
        descriptionText.text = description

        when (type) {
            ChipType.SUCCESS -> {
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_green_dark
                    )
                )
                icon.setImageResource(android.R.drawable.ic_menu_upload)
            }

            ChipType.ERROR -> {
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_red_dark
                    )
                )
                icon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            }

            ChipType.WARNING -> {
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_orange_dark
                    )
                )
                icon.setImageResource(android.R.drawable.ic_menu_info_details)
            }

            ChipType.INFO -> {
                container.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        android.R.color.holo_blue_dark
                    )
                )
                icon.setImageResource(android.R.drawable.ic_menu_info_details)
            }
        }


        Handler(Looper.getMainLooper()).postDelayed({
            try {
                rootView.removeView(chipView)
            } catch (e: Exception) {
                // View might already be removed
            }
        }, 5000)
    }

    enum class ChipType {
        SUCCESS, ERROR, WARNING, INFO
    }

}