package com.example.petly

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class UserProfileActivity : AppCompatActivity() {

    private lateinit var btnOpenAddDialog: RelativeLayout
    private lateinit var btnAddPat: RelativeLayout

    //Firebase
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: PetAdapter
    private val petList = mutableListOf<Pet>()
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        //Firebase and database
        auth = FirebaseAuth.getInstance()

        btnOpenAddDialog = findViewById(R.id.addPetBtn)

        recyclerView = findViewById(R.id.pets)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = PetAdapter(petList)
        recyclerView.adapter = adapter

        dbRef = Firebase.database.reference.child("pets")
        loadUserPets()

        btnOpenAddDialog.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val dialogAddView = layoutInflater.inflate(R.layout.bottom_sheat_add_pets, null)

            btnAddPat = dialogAddView.findViewById(R.id.btnAddDialog)

            btnAddPat.setOnClickListener{
                addPet(dialogAddView)
                dialog.cancel()
            }

            dialog.setContentView(dialogAddView)
            dialog.show()
        }
    }

    private fun addPet(view: View) {
        val txtName = view.findViewById<EditText>(R.id.txtName)
        val txtSpecies = view.findViewById<EditText>(R.id.txtSpecies)
        val txtBreed = view.findViewById<EditText>(R.id.txtBreed)
        val txtWeight = view.findViewById<EditText>(R.id.txtWeight)
        val txtAge = view.findViewById<EditText>(R.id.txtAge)
        val txtInfo = view.findViewById<EditText>(R.id.txtInfo)

        val name = txtName.text.toString().trim()
        val species = txtSpecies.text.toString().trim()
        val breed = txtBreed.text.toString().trim()
        val weight = txtWeight.text.toString().trim()
        val age = txtAge.text.toString().trim()
        val info = txtInfo.text.toString().trim()

        val ownerId = auth.currentUser?.uid ?: ""

        addPetToDatabase(name, species, breed, weight, age, info, ownerId)
    }


    private fun addPetToDatabase(name: String, species: String, breed: String, weight: String, age: String, info: String, ownerId: String) {
        val pet = Pet(name, species, breed, weight, age, info, ownerId)

        val petsRef = Firebase.database.reference.child("pets")

        val newPetRef = petsRef.push()

        newPetRef.setValue(pet).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Pet added successfully!", Toast.LENGTH_SHORT).show()
                loadUserPets()
            } else {
                Toast.makeText(this, "Failed to add pet!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserPets() {
        val userId = auth.currentUser?.uid ?: return

        dbRef.orderByChild("ownerId").equalTo(userId)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    petList.clear()
                    for (petSnapshot in snapshot.children) {
                        val pet = petSnapshot.getValue(Pet::class.java)
                        pet?.let { petList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
