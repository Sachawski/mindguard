package com.example.mindguard.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater.*
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mindguard.R
import com.example.mindguard.data.model.Friend

class FriendsAdapter(private val context : Context, private val friendsList: List<Friend>): ArrayAdapter<Friend>(context, R.layout.friends_layout,friendsList) {

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = from(context).inflate(R.layout.friends_layout,parent,false)
            val friendName = view.findViewById<TextView>(R.id.friend_uuid)
            friendName.text = friendsList[position].name
            return view
        }

        override fun addAll(vararg items: Friend?) {
            super.addAll(*items)
        }



}