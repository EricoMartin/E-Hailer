package com.basebox.ehailerrider.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import com.basebox.ehailerrider.R
import com.basebox.ehailerrider.data.Driver

class AdapterListView(context: Context, val driverList:ArrayList<Driver>): BaseAdapter() {

    private var contxt: Context = context
    private var drivers: ArrayList<Driver> = driverList

    override fun getCount(): Int {
        return drivers.size
    }

    override fun getItem(p0: Int): Any? {
        return null
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        var newP = p1
        newP = LayoutInflater.from(contxt).inflate(R.layout.driver_list, p2, false)
        val img: ImageView = newP.findViewById(R.id.img)
        val name: TextView = newP.findViewById(R.id.driver_name)
        val make: TextView = newP.findViewById(R.id.vehicle_make)
        val reg: TextView = newP.findViewById(R.id.reg_number)

        img.setImageDrawable(contxt.resources.getDrawable(R.drawable.my_pic))

            name.text = driverList[p0].username
            make.text = driverList[p0].carType
            reg.text = driverList[p0].regNum

        newP.setOnClickListener {
            it.findNavController().navigate(R.id.action_driverListFragment_to_awaitingDriverFragment)
        }
        return newP
    }
}