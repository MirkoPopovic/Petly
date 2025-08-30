package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.window.SplashScreen
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private lateinit var profileImg: ImageView
    private lateinit var searchBar: EditText
    private lateinit var filterBtn: ImageView


    private lateinit var btnApplayFitler: RelativeLayout
    private lateinit var btnCancleFilter: RelativeLayout
    private lateinit var citySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getStuffFromXml()

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

        profileImg.setOnClickListener{
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

        getCurrentUser { user ->
            if(user != null){
                loadUsers(user, null)
            }
        }

        filterBtn.setOnClickListener{
            openFilterDialog()
        }

    }

    private fun getStuffFromXml(){
        profileImg = findViewById(R.id.profileImg)
        searchBar = findViewById(R.id.search)
        filterBtn = findViewById(R.id.filterBtn)
    }

    private fun userNotLogedIn(){
        val intent = Intent(this@MainActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun loadUsers(user: User, city: String? = null) {
        val database = FirebaseDatabase.getInstance().getReference("User")
        val targetRole = if (user.role == "owner") "sitter" else "owner"

        database.orderByChild("role").equalTo(targetRole)
            .get()
            .addOnSuccessListener { snapshot ->
                val userList = mutableListOf<User>()
                for (child in snapshot.children) {
                    val u = child.getValue(User::class.java)
                    if (u != null) {
                        if (city == null || u.city.equals(city, ignoreCase = true)) {
                            userList.add(u)
                        }
                    }
                }
                showUsers(userList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


    private fun showUsers(users: List<User>) {
        val recyclerView = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.users)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        recyclerView.adapter = UserAdapter(users) { clickedUser ->
            val intent = Intent(this, VisitProfile::class.java)
            intent.putExtra("USER_ID", clickedUser.uid)
            startActivity(intent)
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

    private fun openFilterDialog(){
        val dialog = BottomSheetDialog(this)
        var dialogAddView = layoutInflater.inflate(R.layout.bottom_sheat_filter, null)

        btnApplayFitler = dialogAddView.findViewById(R.id.btnApplayFilter)
        btnCancleFilter = dialogAddView.findViewById(R.id.btnCancleFilter)
        citySpinner = dialogAddView.findViewById(R.id.spinnerCity)

        setSpinnerData(dialogAddView)

        btnApplayFitler.setOnClickListener {
            val selectedCity = citySpinner.selectedItem.toString()
            getCurrentUser { user ->
                if(user != null){
                    loadUsers(user, selectedCity)
                }
            }
            dialog.dismiss()
        }

        btnCancleFilter.setOnClickListener {
            getCurrentUser { user ->
                if(user != null){
                    loadUsers(user, null)
                }
            }
            dialog.dismiss()
        }

        dialog.setContentView(dialogAddView)
        dialog.show()
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