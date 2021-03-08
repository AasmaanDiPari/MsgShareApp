package com.example.msgshareapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        register.setOnClickListener {
            performRegister()
        }


        Already_account.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        selectPhoto_button.setOnClickListener {
            Log.d("MainActivity", "Try to show photo selected")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)

        }


    }

    private fun performRegister() {

        //val name = name.text.toString()
        val email = user_id.text.toString()
        val password = pswrd.text.toString()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "please Enter email/passward", Toast.LENGTH_SHORT).show()
            return
        }
        // Log.d("Main activity","User name is:$name")
        Log.d("Main activity", "Email is:" + email)
        Log.d("Main activity", "Password is:$password")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener


                Log.d("main", "Successfully added user with uid:${it.result?.user?.uid}")


            }.addOnFailureListener {
                Log.d("main", "Failed to create user${it.message}")
                Toast.makeText(
                    this,
                    "Failed to create user with Email:${it.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }.addOnSuccessListener {
                Toast.makeText(
                    this,
                    "successfully addeed user with Email${email},Password:${password}",
                    Toast.LENGTH_SHORT
                ).show()
                uploadImageToFirebaseStorage()
            }

    }


    var selectphotoURI: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("MainActivity", "Photo was selected")
            selectphotoURI = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectphotoURI)
            circle_imageview.setImageBitmap(bitmap)
            selectPhoto_button.alpha=0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectPhoto_button.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectphotoURI == null) return
        var filename = UUID.randomUUID().toString()
        var ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectphotoURI!!).addOnSuccessListener {
            Log.d("MainActivity", "Successfully uploaded image:${it.metadata?.path}")
            ref.downloadUrl.addOnSuccessListener {

                Log.d("MainActivity", "File location:${it}")
                saveUserToFireDatabase(it.toString())
            }.addOnFailureListener(){
                Log.d("Main","Failed to upload image")
            }
        }
    }
    private fun saveUserToFireDatabase(profileImageUrl:String) {
        val uid=FirebaseAuth.getInstance().uid?:""
        val ref= FirebaseDatabase.getInstance().getReference("/users/$uid")
        var user= User(
            uid,
            name.text.toString(),
            profileImageUrl
        )
        ref.setValue(user).addOnSuccessListener {
            Log.d("MainActivity","Finally added user to firedatabase!")
            val intent=Intent(this, LatestMsgActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }.addOnFailureListener(){
            Log.d("Main","Failed to add to database!!")
        }
    }
}
