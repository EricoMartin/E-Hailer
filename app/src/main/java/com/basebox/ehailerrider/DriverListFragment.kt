package com.basebox.ehailerrider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.basebox.ehailerrider.data.Driver
import com.basebox.ehailerrider.databinding.FragmentDriverListBinding
import com.basebox.ehailerrider.utils.AdapterListView

/**
 * A simple [Fragment] subclass.
 * Use the [DriverListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DriverListFragment : Fragment() {
    private var arrayList: ArrayList<Driver>? = null
    private var listView: ListView? = null
    private var adapter: AdapterListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_driver_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.list)
        showdriverList()
        adapter = AdapterListView(requireContext(), arrayList!!)
        listView!!.adapter = adapter
    }

    private fun showdriverList() {
        arrayList = ArrayList()
        arrayList!!.add(Driver(1, "Daniel", "07083661614", "no 5 tokunbo street", "123456524", "12345", "Toyota Corolla", "XYZ 125 LP" ))
        arrayList!!.add(Driver(2, "Seun", "07083661614", "no 21 Adedayo street", "555456524", "12345", "Toyota Camry", "DEF 125 AP" ))
        arrayList!!.add(Driver(3, "Ekene", "07083661614", "no 11 kofo street", "123456524", "12345", "Hyundai Elantra", "LSP 638 BB" ))
        arrayList!!.add(Driver(4, "Sola", "07083661614", "no 90 tokunbo street", "123456524", "12345", "Toyota Corolla", "XYZ 125 LP" ))
        arrayList!!.add(Driver(5, "Ibrahim", "07083661614", "no 9 ajayi street", "123456524", "12345", "Honda Accord", "OLK 125 HP" ))
        arrayList!!.add(Driver(6, "Oche", "07083661614", "no 27 olubode street", "123456524", "12345", "Honda Civic", "HGD 125 QP" ))
        arrayList!!.add(Driver(7, "Micheal", "07083661614", "no 82 Allen street", "123456524", "12345", "Toyota Corolla", "XYZ 637 AB" ))
        arrayList!!.add(Driver(8, "Afolabi", "07083661614", "no 19 Ojpebi street", "123456524", "12345", "Toyota Corolla", "NMP 12 NM" ))
    }

    companion object {

    }
}