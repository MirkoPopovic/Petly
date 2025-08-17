package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var btnProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.hide()

        findViewById<TextView>(R.id.txtname).setOnClickListener{
            auth.signOut()
            userNotLogedIn()
        }

        btnProfile = findViewById(R.id.btnProfile)

        btnProfile.setOnClickListener{
            /*val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
            startActivity(intent)*/

            getCurrentUser { user ->
                if(user != null) {
                    if(user.role.equals("sitter")){
                        val intent = Intent(this@MainActivity, SitterProfileAcitivity::class.java)
                        startActivity(intent)
                    }
                    else if(user.role.equals("owner")){
                        val intent = Intent(this@MainActivity, UserProfileActivity::class.java)
                        startActivity(intent)
                    }

                }
            }

        }

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            userNotLogedIn()
        }else{
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser == null) {
                        auth.signOut()
                        userNotLogedIn()
                    }
                } else {
                    auth.signOut()
                    userNotLogedIn()
                }
            }
        }
    }

    private fun userNotLogedIn(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun getCurrentUser(callback: (User?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            callback(null)
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)

        userRef.get()
            .addOnSuccessListener { snapshot ->
                val user = if (snapshot.exists()) {
                    snapshot.getValue(User::class.java)
                } else {
                    null
                }
                callback(user)
            }
            .addOnFailureListener {
                callback(null)
            }
    }


}