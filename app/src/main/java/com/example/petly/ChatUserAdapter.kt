package com.example.petly

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatUserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return ChatUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = users.size

    inner class ChatUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val city: TextView = itemView.findViewById(R.id.city)

        fun bind(user: User) {
            userName.text = user.name ?: "No Name"
            city.text = user.city ?: ""

            itemView.setOnClickListener {
                onUserClick(user)
            }
        }
    }
}
