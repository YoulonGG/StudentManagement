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


}