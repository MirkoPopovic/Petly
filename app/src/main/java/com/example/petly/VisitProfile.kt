package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class VisitProfile : AppCompatActivity() {

    private lateinit var txtName: TextView
    private lateinit var txtAboutMe: TextView
    private lateinit var myPetsSection: View
    private lateinit var petsRecyclerView: RecyclerView
    private lateinit var profileImg: ImageView
    private lateinit var btnBack: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_profile)

        txtName = findViewById(R.id.txtName)
        txtAboutMe = findViewById(R.id.txtAboutMe) // About Me tekst
        myPetsSection = findViewById(R.id.myPetsSection)
        petsRecyclerView = findViewById(R.id.pets)
        profileImg = findViewById(R.id.profileImg)
        btnBack = findViewById(R.id.mainHeader)

        val userId = intent.getStringExtra("USER_ID")
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("User").child(userId)
            userRef.get().addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    txtName.text = user.name ?: ""
                    txtAboutMe.text = "Age: ${user.age}\nCity: ${user.city}"

                    if (user.role.equals("sitter", ignoreCase = true)) {
                        myPetsSection.visibility = View.GONE
                    } else {
                        myPetsSection.visibility = View.VISIBLE
                        loadUserPets(user.uid)
                    }

                }
            }
        }

        btnBack.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserPets(userId: String) {
        val petsRef = FirebaseDatabase.getInstance().getReference("Pets").child(userId)
        petsRef.get().addOnSuccessListener { snapshot ->
            val petList = mutableListOf<Pet>()
            for (child in snapshot.children) {
                val pet = child.getValue(Pet::class.java)
                if (pet != null) {
                    petList.add(pet)
                }
            }

            petsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            petsRecyclerView.adapter = PetAdapter(petList)
        }
    }


}
