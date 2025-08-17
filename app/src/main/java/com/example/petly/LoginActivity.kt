package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
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
    private lateinit var txtEmailLogin: EditText
    private lateinit var txtPassworLogin: EditText
    private lateinit var citySpinner: Spinner

    private lateinit var linkLogin: TextView
    private lateinit var btnSingUp: Button
    private lateinit var txtNameSingUp: EditText
    private lateinit var txtEmailSingUp: EditText
    private lateinit var txtPasswordSingUp: EditText
    private lateinit var btnOwner: Button
    private lateinit var btnSitter: Button
    private lateinit var linkBack: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var section: RelativeLayout

    //Boolean for role
    private var owner: Boolean = false
    private var sitter: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //Firebase auth
        auth = FirebaseAuth.getInstance()

        //Role
        owner = false
        sitter = false

        //View for login and singUp
        var loginView = layoutInflater.inflate(R.layout.login_layout, null)
        var roleView = layoutInflater.inflate(R.layout.role_layout, null)
        var singUpView = layoutInflater.inflate(R.layout.singup_layout, null)
        section = findViewById(R.id.loginSection)
        section.addView(roleView)


        //Login
        linkSingUp = loginView.findViewById(R.id.singUpLink)
        btnLogin = loginView.findViewById(R.id.btnLogin)
        txtEmailLogin = loginView.findViewById(R.id.editEmail)
        txtPassworLogin = loginView.findViewById(R.id.editPassword)

        //SingUp
        linkLogin = singUpView.findViewById(R.id.loginLink)
        btnSingUp = singUpView.findViewById(R.id.btnSingUp)
        txtNameSingUp = singUpView.findViewById(R.id.editName)
        txtEmailSingUp = singUpView.findViewById(R.id.editEmail)
        txtPasswordSingUp = singUpView.findViewById(R.id.editPassword)
        linkBack = singUpView.findViewById(R.id.btnBack)
        citySpinner = singUpView.findViewById(R.id.spinnerCity)


        //Role
        btnOwner = roleView.findViewById(R.id.btnOwner)
        btnSitter = roleView.findViewById(R.id.btnSitter)

        btnOwner.setOnClickListener{
            owner = true
            sitter = false
            section.removeView(roleView)
            section.addView(singUpView)
        }

        btnSitter.setOnClickListener{
            owner = false
            sitter = true
            section.removeView(roleView)
            section.addView(singUpView)
        }

        linkSingUp.setOnClickListener{
            section.removeView(loginView)
            section.addView(roleView)
        }

        linkLogin.setOnClickListener{
            section.removeView(singUpView)
            section.addView(loginView)
        }

        linkBack.setOnClickListener{
            owner = false
            sitter = false
            section.removeView(singUpView)
            section.addView(roleView)
        }

        btnLogin.setOnClickListener{
            val email = txtEmailLogin.text.toString()
            val password = txtPassworLogin.text.toString()
            if(email.isNotBlank() && password.isNotBlank())
                login(email, password)
            else
                Toast.makeText(this@LoginActivity, "Obavezno", Toast.LENGTH_SHORT).show()
        }

        btnSingUp.setOnClickListener {
            val name = txtNameSingUp.text.toString()
            val email = txtEmailSingUp.text.toString()
            val password = txtPasswordSingUp.text.toString()
            val city = citySpinner.selectedItem.toString()

            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                signUp(name, email, password, city)
            } else {
                Toast.makeText(this@LoginActivity, "Sva polja su obavezna.", Toast.LENGTH_SHORT).show()
            }
        }
        setSpinnerData(singUpView)
    }

    private fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    var intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUp(name: String, email: String, password: String, city: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if(owner && !sitter)
                        addUserToDatabase(name, email, "owner", auth.uid!!, city)
                    else if(sitter && !owner)
                        addUserToDatabase(name, email, "sitter", auth.uid!!, city)
                    else
                        Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT)
                    var intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, role: String, uid: String, city: String){
        database = Firebase.database.reference
        database.child("User").child(uid).setValue(User(name, email, role, uid, city))
    }

    private fun setSpinnerData(view: View){
        val spinner: Spinner = view.findViewById(R.id.spinnerCity)
        ArrayAdapter.createFromResource(
            this,
            R.array.serbian_cities,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

    }
}