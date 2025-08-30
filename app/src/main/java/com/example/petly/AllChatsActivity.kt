package com.example.petly

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AllChatsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatUserAdapter
    private val userList = mutableListOf<User>()

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_chats)

        recyclerView = findViewById(R.id.recyclerViewChats)
        recyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatUserAdapter(userList) { clickedUser ->
            openChat(clickedUser)
        }
        recyclerView.adapter = chatAdapter

        loadChatUsers()
    }

    private fun loadChatUsers() {
        // Prava referenca
        val database = FirebaseDatabase.getInstance().getReference("Messages").child(currentUserId)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(this@AllChatsActivity, "No chats found for user: $currentUserId", Toast.LENGTH_LONG).show()
                    userList.clear()
                    chatAdapter.notifyDataSetChanged()
                    return
                }

                val usersSet = mutableSetOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { usersSet.add(it) }
                }

                Toast.makeText(this@AllChatsActivity, "Users found: ${usersSet.size}", Toast.LENGTH_SHORT).show()

                if (usersSet.isEmpty()) {
                    userList.clear()
                    chatAdapter.notifyDataSetChanged()
                    return
                }

                userList.clear()
                var loadedUsers = 0

                for (uid in usersSet) {
                    val userRef = FirebaseDatabase.getInstance().getReference("User").child(uid)
                    userRef.get().addOnSuccessListener { userSnapshot ->
                        val user = userSnapshot.getValue(User::class.java)
                        user?.let { userList.add(it) }

                        loadedUsers++
                        if (loadedUsers == usersSet.size) {
                            // Svi korisnici učitani, osvežavamo adapter jednom
                            chatAdapter.notifyDataSetChanged()
                        }
                    }.addOnFailureListener {
                        loadedUsers++
                        Toast.makeText(this@AllChatsActivity, "Failed to load user: ${it.message}", Toast.LENGTH_SHORT).show()
                        if (loadedUsers == usersSet.size) {
                            chatAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AllChatsActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun openChat(user: User) {
        val intent = Intent(this, chatActivity::class.java)
        intent.putExtra("USER_ID", user.uid)
        intent.putExtra("USER_NAME", user.name)
        startActivity(intent)
    }
}
