package com.example.petly

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database

class SitterProfileAcitivity : AppCompatActivity() {

    private lateinit var btnOpenSettingsDialog: RelativeLayout
    private lateinit var btnDeleteAccount: RelativeLayout
    private lateinit var backBtn: RelativeLayout
    private lateinit var btnSignOut: RelativeLayout
    private lateinit var btnEdit: ImageView
    private lateinit var txtAboutMe: TextView

    private lateinit var btnSave: RelativeLayout
    private lateinit var txtUserName: TextView
    private lateinit var txtUsrAge: TextView
    private lateinit var citySpinner: Spinner

    private lateinit var auth: FirebaseAuth
    

    private lateinit var txtName: TextView
    private lateinit var txtDescription: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sitter_profile)

        //Firebase and database
        auth = FirebaseAuth.getInstance()

        getStuffFromXml()

        txtName = findViewById(R.id.txtName)
        txtDescription = findViewById(R.id.aboutMeH)


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
        btnOpenSettingsDialog = findViewById(R.id.settingsBtn)
        txtName = findViewById(R.id.txtName)
        txtDescription = findViewById(R.id.aboutMeH)
        backBtn = findViewById(R.id.mainHeader)
        btnSignOut = findViewById(R.id.logOutBtn)
        btnEdit = findViewById(R.id.btnEdit)
        txtAboutMe = findViewById(R.id.txtAboutMe)
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

        userRef.removeValue()
            .addOnSuccessListener {
                user?.delete()
                    ?.addOnSuccessListener {
                        Toast.makeText(this@SitterProfileAcitivity, "Account deleted", Toast.LENGTH_SHORT).show()
                        FirebaseAuth.getInstance().signOut()

                        val intent = Intent(this@SitterProfileAcitivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(this@SitterProfileAcitivity, "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@SitterProfileAcitivity, "Error deleting user data: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
        citySpinner = dialogAddView.findViewById(R.id.spinnerCity)
        txtUserName = dialogAddView.findViewById(R.id.txtUserName)
        txtUsrAge = dialogAddView.findViewById(R.id.txtUserAge)

        getCurrentUser { user ->
            if(user != null) {
                txtUserName.text = user.name
                txtUsrAge.text = user.age

                val adapter = ArrayAdapter.createFromResource(
                    this,
                    R.array.serbian_cities,
                    android.R.layout.simple_spinner_item
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                citySpinner.adapter = adapter
                val initialCity = user.city
                val position = adapter.getPosition(initialCity)
                citySpinner.setSelection(position)
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
        val updatedCity = citySpinner.selectedItem.toString()
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