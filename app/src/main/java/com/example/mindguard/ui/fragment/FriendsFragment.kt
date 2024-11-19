package com.example.mindguard.ui.fragment

import android.R
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.databinding.FragmentFriendsBinding
import com.example.mindguard.ui.viewmodel.FriendsViewModel
import com.example.mindguard.ui.Tools

class FriendsFragment : Fragment() {

        private var _binding: FragmentFriendsBinding? = null
        private val binding get() = _binding!!
        private val tools = Tools()
        @SuppressLint("SetTextI18n", "RestrictedApi")
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)

            _binding = FragmentFriendsBinding.inflate(inflater, container, false)
            val root: View = binding.root

            val uuidView: TextView = binding.textUuid
            friendsViewModel.uuid.observe(viewLifecycleOwner) {
                uuidView.text = "Your UUID : $it"
            }

            val inputText : EditText = binding.inputUuid
            inputText.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                    actionId == EditorInfo.IME_ACTION_SEND ||
                    actionId == EditorInfo.IME_ACTION_NEXT ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    val text = inputText.text.toString()
                    friendsViewModel.addFriendToUser(text)
                    tools.hideKeyboard(context,binding.inputUuid)
                    true
                } else {
                    false
                }
            }

            val friendListView: ListView = binding.listFriend
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, mutableListOf())
            friendListView.adapter = adapter
            friendsViewModel.friendList.observe(viewLifecycleOwner) { friendList ->
                adapter.clear()
                adapter.addAll(friendList)
                adapter.notifyDataSetChanged()
            }

            return root
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

}