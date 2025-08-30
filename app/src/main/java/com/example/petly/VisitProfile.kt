package com.example.petly

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class VisitProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)

        val userId = intent.getStringExtra("USER_ID")

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("User").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {

                }
            }
        }
    }
}
