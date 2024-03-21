package com.basebox.ehailerrider

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.basebox.ehailerrider.databinding.FragmentOnBoard1Binding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 * Use the [OnBoard1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnBoard1Fragment : Fragment() {
    private lateinit var _binding: FragmentOnBoard1Binding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnBoard1Binding.inflate(layoutInflater)

        FirebaseApp.initializeApp( requireContext())
//        auth = FirebaseAuth.getInstance()
        return _binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = binding.nextButton
        button.setOnClickListener { it.findNavController()
            .navigate(R.id.action_onBoard1Fragment_to_onBoard2Fragment) }
    }

    public override fun onStart() {
        super.onStart()
        // Check if User is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        val regUser = auth.uid
//        if (currentUser == null && regUser == null) {
//            findNavController().navigate(R.id.action_onBoard1Fragment_to_onBoard2Fragment)
//        }else {
//            findNavController().navigate(R.id.mapFragment)
//        }
    }
}