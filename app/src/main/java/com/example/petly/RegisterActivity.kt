package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import org.checkerframework.checker.units.qual.Length

class RegisterActivity : AppCompatActivity() {

    private lateinit var linkLogin: TextView
    private lateinit var btnSingUp: Button
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        auth = FirebaseAuth.getInstance()

        linkLogin = findViewById(R.id.loginLink)
        btnSingUp = findViewById(R.id.btnSingUp)
        txtName = findViewById(R.id.editName)
        txtEmail = findViewById(R.id.editEmail)
        txtPassword = findViewById(R.id.editPassword)

        linkLogin.setOnClickListener{
            var intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }

        btnSingUp.setOnClickListener{
            var name = txtName.text.toString()
            var email = txtEmail.text.toString()
            var password = txtPassword.text.toString()
            signUp(name, email, password)
        }
    }

    private fun signUp(name: String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, email, auth.uid!!)
                    var intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@RegisterActivity, "Error", Toast.LENGTH_SHORT)
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String){
        database = Firebase.database.reference
        database.child("User").child(uid).setValue(User(name, email, uid))
    }
}