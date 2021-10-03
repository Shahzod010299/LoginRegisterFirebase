package com.example.loginregisterfirebase.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.loginregisterfirebase.R
import com.example.loginregisterfirebase.toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.android.synthetic.main.fragment_update_password.*


class UpdatePasswordFragment : Fragment() {
    private val currentuser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutPassword.visibility = View.VISIBLE
        layoutUpdatePassword.visibility = View.GONE

        button_Authenticate.setOnClickListener {
            val password = edit_text_password.text.toString().trim()

            if (password.isEmpty()){
                edit_text_password.error = "Password required"
                edit_text_password.requestFocus()
                return@setOnClickListener
            }

            currentuser?.let { user ->
                val credental = EmailAuthProvider.getCredential(user.email!!,password)

                progressbar.visibility = View.VISIBLE

                user.reauthenticate(credental).addOnCompleteListener { task ->
                    progressbar.visibility = View.GONE

                    when {
                        task.isSuccessful -> {
                            layoutPassword.visibility = View.GONE
                            layoutUpdatePassword.visibility = View.VISIBLE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException -> {
                            edit_text_new_password.error = "Invalid Password"
                            edit_text_new_password.requestFocus()
                        }
                        else -> {
                            context?.toast(task.exception?.message!!)
                        }
                    }
                }
            }


        }
        buttun_update.setOnClickListener { it ->
            val password = edit_text_new_password.text.toString().trim()

            if (password.isEmpty() || password.length < 6){
                edit_text_new_password.error = "Atleast 6 char password required"
                edit_text_new_password.requestFocus()
                return@setOnClickListener
            }

            if (password != edit_text_new_password_confirm.text.toString().trim()){
                edit_text_new_password_confirm.error = "passwrod did not match"
                edit_text_new_password_confirm.requestFocus()
                return@setOnClickListener
            }

            currentuser?.let { user ->

                progressbar.visibility = View.VISIBLE

                user.updatePassword(password).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val action = UpdatePasswordFragmentDirections.actionPasswordUpdate()
                        Navigation.findNavController(it).navigate(action)
                        context?.toast("Password Updated")
                    }else{
                        context?.toast(task.exception?.message!!)
                    }
                }

            }

        }

    }

}