package com.basebox.ehailerrider

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.basebox.ehailerrider.databinding.FragmentAwaitingDriverBinding

private const val TAG = "AwaitingDriverFragment"

/**
 * A simple [Fragment] subclass.
 * Use the [AwaitingDriverFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AwaitingDriverFragment : Fragment() {

    private lateinit var _binding: FragmentAwaitingDriverBinding
    private val binding get() = _binding
    private var num:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAwaitingDriverBinding.inflate(inflater, container,false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            nextButton.setOnClickListener {
                val intentCallDriver = Intent(Intent.ACTION_CALL)
                num = "08100058616"
                intentCallDriver.setData(Uri.parse("tel: $num"))
                if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermission()
                } else {
                    startActivity(intentCallDriver)
                }
            }
            chronometer.base= SystemClock.elapsedRealtime()
            chronometer.start()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CALL_PHONE)
    , 1)
    }

    companion object {

    }
}