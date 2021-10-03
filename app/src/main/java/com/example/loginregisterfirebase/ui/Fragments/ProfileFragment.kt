package com.example.loginregisterfirebase.ui.Fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.example.loginregisterfirebase.R
import com.example.loginregisterfirebase.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayOutputStream
import java.util.*


class ProfileFragment : Fragment() {
    private val DEFAULT_IMAGE_URL = "https://picsum.photos/200"

    private val REQUEST_IMAGE_CAPTUR = 100

    private lateinit var imageUrl: Uri

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUser?.let { user ->

            Glide.with(this)
                .load(user.photoUrl)
                .into(image_view)

            edit_text_name.setText(user.displayName)
            text_email.text = user.email

            if (user.isEmailVerified){
                text_not_verified.visibility = View.INVISIBLE
            }else{
                text_not_verified.visibility = View.VISIBLE
            }

            text_phone.text = if (user.phoneNumber.isNullOrEmpty())
                "Add Number"
            else
                user.phoneNumber

        }

        image_view.setOnClickListener {
            takePickerIntent()
        }

        button_save.setOnClickListener {

            val photo = when{
                ::imageUrl.isInitialized -> imageUrl
                currentUser?.photoUrl == null -> Uri.parse(DEFAULT_IMAGE_URL)
                else -> currentUser.photoUrl
            }

            val name = edit_text_name.text.toString().trim()

            if (name.isEmpty()){
                edit_text_name.error = "name required"
                edit_text_name.requestFocus()
                return@setOnClickListener
            }

            val updates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photo).build()

            progressbar.visibility = View.VISIBLE
            currentUser?.updateProfile(updates)?.addOnCompleteListener { task ->
                progressbar.visibility = View.INVISIBLE

                if (task.isSuccessful){
                    context?.toast("Profile updated")
                }else{
                    context?.toast(task.exception?.message!!)
                }
            }

        }

        text_not_verified.setOnClickListener {
            currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    context?.toast("Verification Email send")
                }else{
                    context?.toast(task.exception?.message!!)
                }

            }
        }

        text_phone.setOnClickListener {
            val action = ProfileFragmentDirections.actionVerifyFragment()
            Navigation.findNavController(it).navigate(action)
        }

        text_email.setOnClickListener {
            val action = ProfileFragmentDirections.actionUpdateEmail()
            Navigation.findNavController(it).navigate(action)
        }

        text_password.setOnClickListener {
            val action = ProfileFragmentDirections.actionUpdatePassword()
            Navigation.findNavController(it).navigate(action)
        }
    }

    private fun takePickerIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { pickerIntetn ->
            pickerIntetn.resolveActivity(activity?.packageManager!!).also {
                startActivityForResult(pickerIntetn,REQUEST_IMAGE_CAPTUR)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CAPTUR && resultCode == RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImageAndSaveUri(imageBitmap)
        }
    }

    private fun uploadImageAndSaveUri(bitmap: Bitmap) {

        val boas = ByteArrayOutputStream()

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("pick/${FirebaseAuth.getInstance().currentUser?.uid}")

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,boas)
        val image = boas.toByteArray()

        val upload = storageRef.putBytes(image)

        progressbar_pic.visibility = View.VISIBLE

        upload.addOnCompleteListener{ uploadTask ->
            progressbar_pic.visibility = View.INVISIBLE

            if (uploadTask.isSuccessful){
                storageRef.downloadUrl.addOnCompleteListener {  urlTask ->
                    urlTask.result?.let {  it ->
                        imageUrl = it
                        activity?.toast(imageUrl.toString())
                        image_view.setImageBitmap(bitmap)
                    }
                }
            }else{
                uploadTask.exception?.let {
                    activity?.toast(it.message!!)
                }
            }
        }

    }

}