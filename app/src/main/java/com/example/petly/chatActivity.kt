package com.example.petly

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class chatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var txtUserName: TextView
    private lateinit var btnBack: ImageView
    private lateinit var rootLayout: View

    private lateinit var messagesList: MutableList<Message>
    private lateinit var adapter: MessageAdapter

    private lateinit var database: DatabaseReference
    private lateinit var currentUserId: String
    private lateinit var chatUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.recyclerViewMessages)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        txtUserName = findViewById(R.id.txtUserName)
        btnBack = findViewById(R.id.btnBack)
        rootLayout = findViewById(R.id.rootLayout)

        messagesList = mutableListOf()
        adapter = MessageAdapter(messagesList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatUserId = intent.getStringExtra("USER_ID") ?: ""

        loadChatUserInfo()
        loadMessages()

        buttonSend.setOnClickListener {
            val msg = editTextMessage.text.toString().trim()
            if (msg.isNotEmpty()) sendMessage(msg)
        }

        btnBack.setOnClickListener { finish() }

        editTextMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                val msg = editTextMessage.text.toString().trim()
                if (msg.isNotEmpty()) sendMessage(msg)
                true
            } else false
        }

        // Scroll i padding za notch i navigaciju
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, insets ->
            view.setPadding(
                0,
                insets.systemWindowInsetTop,
                0,
                insets.systemWindowInsetBottom
            )
            recyclerView.scrollToPosition(messagesList.size - 1)
            insets
        }

        // Automatsko skrolovanje kada tastatura bude otvorena
        rootLayout.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootLayout.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootLayout.height
            val keypadHeight = screenHeight - rect.bottom
            if (keypadHeight > screenHeight * 0.15) {
                recyclerView.scrollToPosition(messagesList.size - 1)
            }
        }
    }

    private fun loadChatUserInfo() {
        val userRef = FirebaseDatabase.getInstance().getReference("User").child(chatUserId)
        userRef.get().addOnSuccessListener { snapshot ->
            val user = snapshot.getValue(User::class.java)
            if (user != null) txtUserName.text = user.name
        }
    }

    private fun loadMessages() {
        database = FirebaseDatabase.getInstance().getReference("Messages")
        database.child(currentUserId).child(chatUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()
                    for (child in snapshot.children) {
                        val message = child.getValue(Message::class.java)
                        message?.let { messagesList.add(it) }
                    }
                    adapter.notifyDataSetChanged()
                    recyclerView.scrollToPosition(messagesList.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendMessage(text: String) {
        val msgRef = FirebaseDatabase.getInstance().getReference("Messages")
        val message = Message(
            senderId = currentUserId,
            receiverId = chatUserId,
            message = text,
            timestamp = System.currentTimeMillis()
        )

        val key1 = msgRef.child(currentUserId).child(chatUserId).push().key
        val key2 = msgRef.child(chatUserId).child(currentUserId).push().key

        if (key1 != null && key2 != null) {
            msgRef.child(currentUserId).child(chatUserId).child(key1).setValue(message)
            msgRef.child(chatUserId).child(currentUserId).child(key2).setValue(message)
            editTextMessage.text.clear()
            recyclerView.scrollToPosition(messagesList.size - 1)
        }
    }
}
