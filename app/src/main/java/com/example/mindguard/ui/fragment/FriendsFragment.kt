package com.example.mindguard.ui.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mindguard.databinding.FragmentFriendsBinding
import com.example.mindguard.ui.viewmodel.FriendsViewModel

class FriendsFragment : Fragment() {

        private var _binding: FragmentFriendsBinding? = null

        // This property is only valid between onCreateView and
        // onDestroyView.
        private val binding get() = _binding!!

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val friendsViewModel = ViewModelProvider(this).get(FriendsViewModel::class.java)

            _binding = FragmentFriendsBinding.inflate(inflater, container, false)
            val root: View = binding.root

            val textView: TextView = binding.textFriends
            friendsViewModel.text.observe(viewLifecycleOwner) {
                textView.text = it
            }

            val friendListView: ListView = binding.listFriend
            val adapter = ArrayAdapter<String>(requireContext(), R.layout.list_content, mutableListOf())

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