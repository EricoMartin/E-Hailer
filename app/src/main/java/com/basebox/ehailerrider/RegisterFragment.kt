package com.basebox.ehailerrider

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.basebox.ehailerrider.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


private const val TAG = "RegisterFragment"
/**
 * A [RegisterFragment] subclass.
 * Registers new users on the App
 */
class RegisterFragment : Fragment() {

//    private lateinit var auth: FirebaseAuth
    private lateinit var _binding: FragmentRegisterBinding
    private val binding get() = _binding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2
    private var showOneTapUI = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        return _binding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding.button2.setOnClickListener {
            register()
        }
        _binding.button3.setOnClickListener {
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.your_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(true)
                        .build()
                )
                .build()
        }
        binding.textViewLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
    override fun onStart() {
        super.onStart()
        // Check if User is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            // Got an ID token from Google. Use it to authenticate
                            // with Firebase.
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(requireActivity()) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in User's information
                                        Log.d(TAG, "signInWithCredential:success")
                                        val user = auth.currentUser
                                        updateUI(user)
                                    } else {
                                        // If sign in fails, display a message to the User.
                                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                                        updateUI(null)
                                    }
                                }
                            Log.d(TAG, "Got ID token.")
                        }
                        else -> {
                            // Shouldn't happen.
                            Log.d(TAG, "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    // ...
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun register() {
        System.out.println("The email: ${binding.editTextEmail.text.toString()}")
        System.out.println("The password: ${binding.editTextPass.text.toString()}")
            try {
                auth.createUserWithEmailAndPassword(binding.editTextEmail.text.toString(), binding.editTextPass.text.toString())
                    .addOnCompleteListener(requireContext().mainExecutor) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in User's information
                            Log.d(TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the User.
                            Log.w(TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(context, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error with your credentials", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateUI(user: FirebaseUser?): Any {
        if (user != null) {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

////                Google Auth
//            with(binding) {
//                button3.setOnClickListener {
//                    findNavController().navigate(R.id.action_registerFragment_to_mapFragment)
//                }
//            }
        }


        return Any()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null!!
//    }

}