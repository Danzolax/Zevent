package com.zolax.zevent.util

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object DialogUtil {
    fun buildConfirmDialog(context: Context, message: String, positiveAction: DialogInterface.OnClickListener) : AlertDialog {
        val builder: AlertDialog.Builder =  AlertDialog.Builder(context)
        builder.setTitle("Подтверждение")
        builder.setMessage(message)
        builder.setPositiveButton(android.R.string.ok,positiveAction)
        builder.setNegativeButton(android.R.string.no) { _, _ ->

        }
        builder.setCancelable(true)
        return builder.create()
    }
}