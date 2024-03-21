package com.basebox.ehailerrider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.basebox.ehailerrider.databinding.FragmentOnBoard1Binding
import com.basebox.ehailerrider.databinding.FragmentOnBoard2Binding


/**
 * A simple [Fragment] subclass.
 * Use the [OnBoard2Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnBoard2Fragment : Fragment() {

    private lateinit var _binding: FragmentOnBoard2Binding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnBoard2Binding.inflate(layoutInflater)
        return _binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = binding.nextButton
        button.setOnClickListener { it.findNavController()
            .navigate(R.id.action_onBoard2Fragment_to_registerFragment) }
    }

}