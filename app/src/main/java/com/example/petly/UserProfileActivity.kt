package com.example.petly

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import android.widget.TextView
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.installations.installations
import kotlinx.coroutines.tasks.await


class UserProfileActivity: AppCompatActivity() {

    private lateinit var btnOpenAddDialog: RelativeLayout
    private lateinit var btnAddPat: RelativeLayout
    private lateinit var btnOpenSettingsDialog: RelativeLayout
    private lateinit var btnDeleteAccount: RelativeLayout
    private lateinit var backBtn: RelativeLayout
    private lateinit var btnSignOut: RelativeLayout
    private lateinit var btnEdit: ImageView
    private lateinit var txtAboutMe: TextView

    //Edit profile dilaog
    private lateinit var btnSave: RelativeLayout
    private lateinit var txtUserName: TextView
    private lateinit var txtUsrAge: TextView
    private lateinit var txtUserCity: TextView

    //Firebase
    private lateinit var auth: FirebaseAuth

    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var adapter: PetAdapter
    private val petList = mutableListOf<Pet>()
    private lateinit var petsRef: DatabaseReference

    private lateinit var txtName: TextView
    private lateinit var txtDescription: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        //Firebase and database
        auth = FirebaseAuth.getInstance()



        getStuffFromXml()

        petsRef = Firebase.database.reference.child("pets")
        loadUserPets()


        txtName = findViewById(R.id.txtName)
        txtDescription = findViewById(R.id.aboutMeH)

        btnOpenAddDialog.setOnClickListener {
            openAddPetDialog()
        }

        btnOpenSettingsDialog.setOnClickListener{
            btnOpenSettingsDialog()
        }

        backBtn.setOnClickListener{
            back()
        }

        btnSignOut.setOnClickListener{
            logOut()
        }

        btnEdit.setOnClickListener(){
            openEditDialog()
        }

        loadUserData()
    }

    private fun getStuffFromXml(){
        btnOpenAddDialog = findViewById(R.id.addPetBtn)
        btnOpenSettingsDialog = findViewById(R.id.settingsBtn)

        recyclerView = findViewById(R.id.pets)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = PetAdapter(petList)
        recyclerView.adapter = adapter

        txtName = findViewById(R.id.txtName)
        txtDescription = findViewById(R.id.aboutMeH)

        backBtn = findViewById(R.id.mainHeader)

        btnSignOut = findViewById(R.id.logOutBtn)
        btnEdit = findViewById(R.id.btnEdit)
        txtAboutMe = findViewById(R.id.txtAboutMe)
    }

    private fun back(){
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun logOut(){
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openEditDialog(){
        val dialog = BottomSheetDialog(this)
        var dialogAddView = layoutInflater.inflate(R.layout.edit_profile, null)

        btnSave = dialogAddView.findViewById(R.id.btnSaveUser)
        txtUserCity = dialogAddView.findViewById(R.id.txtUserCity)
        txtUserName = dialogAddView.findViewById(R.id.txtUserName)
        txtUsrAge = dialogAddView.findViewById(R.id.txtUserAge)

        getCurrentUser { user ->
            if(user != null) {
                txtUserName.text = user.name
                txtUserCity.text = user.city
                txtUsrAge.text = user.age
            }
        }

        btnSave.setOnClickListener{
            updateUser(dialog)
        }

        dialog.setContentView(dialogAddView)
        dialog.show()
    }

    private fun updateUser(dialog: BottomSheetDialog){
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)

        val updatedName = txtUserName.text.toString().trim()
        val updatedCity = txtUserCity.text.toString().trim()
        val updatedAge = txtUsrAge.text.toString().trim()

        val updates = mapOf(
            "name" to updatedName,
            "city" to updatedCity,
            "age" to updatedAge
        )

        userRef.updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profil ažuriran", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                loadUserData()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Greška pri ažuriranju: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openAddPetDialog(){
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

    private fun btnOpenSettingsDialog(){
        val dialog = BottomSheetDialog(this)
        val dialogAddView = layoutInflater.inflate(R.layout.botton_sheat_delete_account, null)

        btnDeleteAccount = dialogAddView.findViewById(R.id.btnDeleteAccount)

        btnDeleteAccount.setOnClickListener{
            showDeleteAccountDialog()
            dialog.cancel()
        }

        dialog.setContentView(dialogAddView)
        dialog.show()
    }

    private fun showDeleteAccountDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Enter your password"

        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("This action is permanent. Please confirm your password.")
            .setView(input)
            .setPositiveButton("Delete") { _, _ ->
                val password = input.text.toString()
                if (password.isNotEmpty()) {
                    reauthenticateAndDelete(password)
                } else {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun reauthenticateAndDelete(password: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email

        if (user != null && email != null) {
            val credential = EmailAuthProvider.getCredential(email, password)

            user.reauthenticate(credential)
                .addOnSuccessListener {
                    deleteAccount()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Re-authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

    }

    private fun deleteAccount() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return

        val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
        val petsRef = FirebaseDatabase.getInstance().getReference("pets")

        // 1. Prvo brišemo sve ljubimce tog korisnika
        petsRef.orderByChild("ownerId").equalTo(uid)
            .addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (petSnapshot in snapshot.children) {
                        petSnapshot.ref.removeValue()
                    }

                    // 2. Brišemo korisnika iz "users" čvora
                    userRef.removeValue()

                    // 3. Brišemo korisnički nalog iz Authentication
                    user?.delete()
                        ?.addOnSuccessListener {
                            Toast.makeText(this@UserProfileActivity, "Account and pets deleted", Toast.LENGTH_SHORT).show()
                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this@UserProfileActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        ?.addOnFailureListener { e ->
                            Toast.makeText(this@UserProfileActivity, "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    Toast.makeText(this@UserProfileActivity, "Failed to delete pets: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
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

        petsRef.orderByChild("ownerId").equalTo(userId)
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

    private fun loadUserData() {
        getCurrentUser { user ->
            if(user != null){
                txtName.text = user.name
                txtAboutMe.text = "Age: " + user.age + "\n" + "City: " + user.city
            }
        }
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
