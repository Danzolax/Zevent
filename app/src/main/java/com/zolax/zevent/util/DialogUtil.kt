package com.zolax.zevent.util

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.zolax.zevent.R

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

    fun buildDialogWithView(context: Context, title: String, view: View, positiveAction: DialogInterface.OnClickListener) : AlertDialog {
        val builder: AlertDialog.Builder =  AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setPositiveButton(android.R.string.ok,positiveAction)
        builder.setNegativeButton(android.R.string.no) { _, _ ->
        }
        builder.setView(view)
        builder.setCancelable(true)
        return builder.create()
    }


}