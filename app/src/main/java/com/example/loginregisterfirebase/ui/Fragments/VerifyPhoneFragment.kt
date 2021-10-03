package com.example.loginregisterfirebase.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.loginregisterfirebase.R
import com.example.loginregisterfirebase.toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_verify_phone.*
import java.util.concurrent.TimeUnit


class VerifyPhoneFragment : Fragment() {
    private var verificationId : String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify_phone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        layoutPhone.visibility = View.VISIBLE
        layoutVefication.visibility = View.GONE

        button_send_verification.setOnClickListener {
            val phone = edit_text_phone.text.toString().trim()

            if (phone.isEmpty() || phone.length != 9){
                edit_text_phone.error = "Enter a valid phone"
                edit_text_phone.requestFocus()
                return@setOnClickListener
            }

            val phoneNumber = '+' + cpp.selectedCountryCode + phone

            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(
                    phoneNumber,
                    60,
                    TimeUnit.SECONDS,
                    requireActivity(),
                    phoneAuthCallback

                )


            layoutPhone.visibility = View.GONE
            layoutVefication.visibility = View.VISIBLE


        }

        button_verify.setOnClickListener {
            val code = edit_text_code.text.toString().trim()

            if (code.isEmpty()){
                edit_text_code.error = "Cod required"
                edit_text_code.requestFocus()
                return@setOnClickListener
            }

            verificationId?.let {
                val credential = PhoneAuthProvider.getCredential(it,code)
                addPhoneNumber(credential)
            }
        }

    }

    private val phoneAuthCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(phoneAuthCredental: PhoneAuthCredential) {
            addPhoneNumber(phoneAuthCredental)
        }

        override fun onVerificationFailed(exction: FirebaseException) {
            context?.toast(exction.message!!)
        }

        override fun onCodeSent(verificationId: String, PhoneAuthProvider: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(verificationId, PhoneAuthProvider)
            this@VerifyPhoneFragment.verificationId = verificationId
        }

    }

    private fun addPhoneNumber(phoneAuthCredental: PhoneAuthCredential) {
        FirebaseAuth.getInstance().currentUser?.updatePhoneNumber(phoneAuthCredental)?.addOnCompleteListener { task ->

            if (task.isSuccessful){
                val action = VerifyPhoneFragmentDirections.actionPhoneFragment()
                Navigation.findNavController(button_verify).navigate(action)
            }else{
                context?.toast(task.exception?.message!!)
            }

        }
    }

}