package com.basebox.ehailerrider.ui.viewmodel

import android.animation.ObjectAnimator
import android.os.Build
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.basebox.ehailerrider.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope

class LoadingDialog(val mFrag: Fragment) {
    private lateinit var dialog: AlertDialog
    @RequiresApi(Build.VERSION_CODES.O)
    fun startLoading(){
        val inflate = mFrag.layoutInflater
        val dialogView = inflate.inflate(R.layout.places_layout, null)
        val builder = AlertDialog.Builder(mFrag.requireContext())
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog.show()
        var progbar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        progbar.max = 10
        val currProgress = 10
        ObjectAnimator.ofInt(progbar,"progress", currProgress)
            .setDuration(3000)
            .start()

    }
    fun isDismissed(){
//        mFrag.findNavController().navigate(R.id.action_mapFragment_to_driverListFragment)
        dialog.dismiss()
    }
}