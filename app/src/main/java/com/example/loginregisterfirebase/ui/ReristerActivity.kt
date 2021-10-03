package com.example.loginregisterfirebase.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.example.loginregisterfirebase.R
import com.example.loginregisterfirebase.login
import com.example.loginregisterfirebase.toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_rerister.*

class ReristerActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rerister)

        mAuth = FirebaseAuth.getInstance()

        btn_sign_in.setOnClickListener{
            val email = edit_email.text.toString().trim()
            val password = edit_password.text.toString().trim()

            if (email.isEmpty()){
                edit_email.error = "Email required"
                edit_email.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edit_email.error = "Valid email required"
                edit_email.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty() || password.length < 6){
                edit_password.error = "6 char password required"
                edit_password.requestFocus()
                return@setOnClickListener
            }

            registerUser(email,password)
        }

        text_view_register.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
    private fun registerUser(email: String,password: String){
        progressbar.visibility = View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                progressbar.visibility = View.GONE

                if (task.isSuccessful){
                   login()
                }else{
                    task.exception?.message?.let{ it ->
                        toast(it)
                    }
                }
            }
    }
    override fun onStart() {
        super.onStart()
        mAuth.currentUser?.let {
            login()
        }
    }
}