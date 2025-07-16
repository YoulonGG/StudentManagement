package com.example.studentmanagement.core.ui_components

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
        buttonText: String = "Okay",
        onBtnClick: () -> Unit
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
            onBtnClick.invoke()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (context.resources.displayMetrics.widthPixels * 0.75).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }


    /**
     * Show a custom dialog with two action buttons
     * @param context Context (Activity/Fragment context)
     * @param title Dialog title
     * @param description Dialog description
     * @param positiveButtonText Positive button text (default: "Yes")
     * @param negativeButtonText Negative button text (default: "No")
     * @param onPositiveClick Callback for positive button click
     * @param onNegativeClick Callback for negative button click (optional)
     */
    fun showTwoButtonDialog(
        context: Context,
        title: String,
        description: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.custom_two_button_dialog, null)

        val txtTitle = view.findViewById<TextView>(R.id.dialog_title)
        val txtDescription = view.findViewById<TextView>(R.id.dialog_description)
        val btnPositive = view.findViewById<Button>(R.id.dialog_btn_positive)
        val btnNegative = view.findViewById<Button>(R.id.dialog_btn_negative)

        txtTitle.text = title
        txtDescription.text = description
        btnPositive.text = positiveButtonText
        btnNegative.text = negativeButtonText

        btnPositive.setOnClickListener {
            onPositiveClick.invoke()
            dialog.dismiss()
        }

        btnNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val width = (context.resources.displayMetrics.widthPixels * 0.75).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        dialog.show()
    }


}