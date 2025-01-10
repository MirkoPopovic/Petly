package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.GetPasswordOption
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class LoginActivity : AppCompatActivity() {

    private lateinit var linkSingUp: TextView
    private lateinit var btnLogin: Button
    private lateinit var txtEmail: EditText
    private lateinit var txtPasswor: EditText

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()


        linkSingUp = findViewById(R.id.singUpLink)
        btnLogin = findViewById(R.id.btnLogin)
        txtEmail = findViewById(R.id.editEmail)
        txtPasswor = findViewById(R.id.editPassword)

        linkSingUp.setOnClickListener{
            var intent = Intent(this, RegisterActivity::class.java)
            finish()
            startActivity(intent)
        }

        btnLogin.setOnClickListener{
            var email = txtEmail.text.toString()
            var password = txtPasswor.text.toString()
            login(email, password)
        }
    }

    private fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT)
                }
            }
    }
}