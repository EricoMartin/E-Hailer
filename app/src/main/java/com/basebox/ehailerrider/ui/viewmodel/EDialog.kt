package com.basebox.ehailerrider.ui.viewmodel

import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat.ThemeCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.basebox.ehailerrider.R

private const val TAG = "EDialog"
class EDialog() : DialogFragment() {
    val loadingDialog = LoadingDialog(this)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        var builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmation")
        builder.setMessage("Place Trip Order?")
        val posBtn = builder.setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
            loadingDialog.startLoading()
            val handler = Handler()
            handler.postDelayed(object: Runnable{
                override fun run() {
                    loadingDialog.isDismissed()
                }
            }, 5000)
            Log.d(TAG, "User Confirmed Trip")
        })
        val negBtn = builder.setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
            Log.d(TAG, "User Canceled Trip")
        })

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(requireContext()
                    .resources.getColor(R.color.purple_500, resources.newTheme()))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(requireContext()
                .resources.getColor(R.color.purple_500, resources.newTheme()))
        }
        return alertDialog
    }
}